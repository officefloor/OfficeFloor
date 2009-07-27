/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.source.HttpStatus;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.synchronise.SynchronizedOutputBufferStream;

/**
 * {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseImpl implements HttpResponse {

	/**
	 * HTTP end of line sequence (CR, LF).
	 */
	private static final String EOL = "\r\n";

	/**
	 * Name of the header Content-Length.
	 */
	private static final String HEADER_NAME_CONTENT_LENGTH = "Content-Length";

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * {@link BufferStream} containing the body content.
	 */
	private final BufferStream body;

	/**
	 * {@link SynchronizedOutputBufferStream} providing access for application
	 * to write body.
	 */
	private final SynchronizedOutputBufferStream safeOutputBufferStream;

	/**
	 * Version.
	 */
	private String version;

	/**
	 * Status code.
	 */
	private int status;

	/**
	 * Status message.
	 */
	private String statusMessage;

	/**
	 * Headers.
	 */
	private final List<HttpHeader> headers = new LinkedList<HttpHeader>();

	/**
	 * Initiate by defaulting from the {@link HttpRequest}.
	 *
	 * @param connection
	 *            {@link Connection}.
	 * @param squirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @param httpVersion
	 *            HTTP version.
	 */
	public HttpResponseImpl(Connection connection,
			BufferSquirtFactory squirtFactory, String httpVersion) {
		this.connection = connection;

		// Create the body buffer
		this.body = new BufferStreamImpl(squirtFactory);
		this.safeOutputBufferStream = new SynchronizedOutputBufferStream(
				this.body.getOutputBufferStream(), this.connection.getLock());

		// Specify initial values
		this.version = httpVersion;
		this.status = HttpStatus._200;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);
	}

	/**
	 * Flags failure in processing the {@link HttpRequest}.
	 *
	 * @param failure
	 *            Failure in processing the {@link HttpRequest}.
	 */
	void flagFailure(Throwable failure) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	/**
	 * Writes the {@link HttpResponse} to the {@link Connection}.
	 *
	 * @throws IOException
	 *             If fails to load the content.
	 */
	void write() throws IOException {
		synchronized (this.connection.getLock()) {

			// Obtain the output buffer stream
			OutputBufferStream output = this.connection.getOutputBufferStream();

			// Create temporary buffer
			byte[] tempBuffer = new byte[1];

			// Provide the content length
			long contentLength = this.body.available();
			this.headers.add(new HttpHeaderImpl(HEADER_NAME_CONTENT_LENGTH,
					String.valueOf(contentLength)));

			// Ensure appropriate successful status
			if ((contentLength == 0) && (this.status == HttpStatus._200)) {
				this.setStatus(HttpStatus._204);
			}

			// Write the status line
			writeUsAscii(output, tempBuffer, this.version);
			writeUsAscii(output, tempBuffer, " ");
			writeUsAscii(output, tempBuffer, String.valueOf(this.status));
			writeUsAscii(output, tempBuffer, " ");
			writeUsAscii(output, tempBuffer, this.statusMessage);
			writeUsAscii(output, tempBuffer, EOL);

			// Write the headers
			for (HttpHeader header : this.headers) {
				writeUsAscii(output, tempBuffer, header.getName());
				writeUsAscii(output, tempBuffer, ": ");
				writeUsAscii(output, tempBuffer, header.getValue());
				writeUsAscii(output, tempBuffer, EOL);
			}
			writeUsAscii(output, tempBuffer, EOL);

			// Write the body
			InputBufferStream bodyInputBufferStream = this.body
					.getInputBufferStream();
			while (contentLength > Integer.MAX_VALUE) {
				bodyInputBufferStream.read(Integer.MAX_VALUE, output);
				contentLength -= Integer.MAX_VALUE;
			}
			bodyInputBufferStream.read((int) contentLength, output);

			// Close the body (can not add further content)
			bodyInputBufferStream.close();
		}
	}

	/**
	 * Writes the text as US-ASCII to the {@link OutputBufferStream}.
	 *
	 * @param outputBufferStream
	 *            {@link OutputBufferStream}.
	 * @param tempBuffer
	 *            Temporary buffer for writing content.
	 * @param text
	 *            Text to be written.
	 * @throws IOException
	 *             If fails to write.
	 */
	private static void writeUsAscii(OutputBufferStream outputBufferStream,
			byte[] tempBuffer, String text) throws IOException {

		// Iterate over the characters writing them
		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);

			// Transform character to byte
			tempBuffer[0] = (byte) character;

			// Write byte to the output buffer stream
			outputBufferStream.write(tempBuffer, 0, 1);
		}
	}

	/*
	 * ================ HttpResponse =======================================
	 */

	@Override
	public void setVersion(String version) {
		synchronized (this.connection.getLock()) {
			this.version = version;
		}
	}

	@Override
	public void setStatus(int status) {
		synchronized (this.connection.getLock()) {
			this.status = status;
			this.statusMessage = HttpStatus.getStatusMessage(status);
		}
	}

	@Override
	public void setStatus(int status, String statusMessage) {
		synchronized (this.connection.getLock()) {
			this.status = status;
			this.statusMessage = statusMessage;
		}
	}

	@Override
	public void addHeader(String name, String value) {
		synchronized (this.connection.getLock()) {
			// Ignore specifying content length
			if (HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(name)) {
				return;
			}

			// Add the header
			this.headers.add(new HttpHeaderImpl(name, value));
		}
	}

	@Override
	public OutputBufferStream getBody() {
		return this.safeOutputBufferStream;
	}

}