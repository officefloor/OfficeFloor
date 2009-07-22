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
package net.officefloor.plugin.socket.server.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseImpl implements HttpResponse {

	/**
	 * US-ASCII {@link Charset}.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * HTTP end of line sequence.
	 */
	public static final String EOL = "\r\n";

	/**
	 * Name of the header Content-Length.
	 */
	public static final String HEADER_NAME_CONTENT_LENGTH = "Content-Length";

	/**
	 * {@link HttpConnectionHandler}.
	 */
	private final HttpConnectionHandler connectionHandler;

	/**
	 * Status code.
	 */
	private int status;

	/**
	 * Status message.
	 */
	private String statusMessage;

	/**
	 * Version.
	 */
	private String version;

	/**
	 * Headers.
	 */
	private final List<Header> headers = new LinkedList<Header>();

	/**
	 * {@link OutputStream} to write the body.
	 */
	private final HttpOutputStream bodyOutputStream = new HttpOutputStream();

	/**
	 * Listing of {@link ByteBuffer} instances that comprise the body.
	 */
	private final List<ByteBuffer> body = new LinkedList<ByteBuffer>();

	/**
	 * Current {@link ByteBuffer} to fill out for the body.
	 */
	private ByteBuffer currentBodyBuffer = null;

	/**
	 * Flag indicating if this {@link HttpResponse} has been triggered to be
	 * sent.
	 */
	private boolean isSent = false;

	/**
	 * Failure.
	 */
	private Throwable failure = null;

	/**
	 * Initiate by defaulting from the {@link HttpRequest}.
	 *
	 * @param connectionHandler
	 *            {@link HttpConnectionHandler}.
	 * @param request
	 *            {@link HttpRequest}.
	 */
	public HttpResponseImpl(HttpConnectionHandler connectionHandler,
			HttpRequest request) {
		this.connectionHandler = connectionHandler;
		this.status = HttpStatus._200;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);
		this.version = request.getVersion();
	}

	/**
	 * Initiate a complete {@link HttpResponse} to be sent.
	 *
	 * @param connectionHandler
	 *            {@link HttpConnectionHandler}.
	 * @param version
	 *            HTTP version.
	 * @param status
	 *            HTTP status.
	 * @param body
	 *            Body text explaining the cause.
	 * @throws IOException
	 *             If fails to construct {@link HttpResponse}.
	 */
	public HttpResponseImpl(HttpConnectionHandler connectionHandler,
			String version, int status, String body) throws IOException {
		this.connectionHandler = connectionHandler;
		this.version = version;
		this.status = status;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);

		// Only write the body if have text
		if ((body != null) && (body.length() > 0)) {
			Writer writer = new OutputStreamWriter(this.bodyOutputStream,
					US_ASCII);
			writer.append(body);
			writer.flush();
		}
	}

	/**
	 * Flags to send failure caused by the escalation.
	 *
	 * @param escalation
	 *            Escalation.
	 * @throws IOException
	 *             If fails to send escalation.
	 */
	public synchronized void sendFailure(Throwable escalation)
			throws IOException {
		// Flag the failure in sending
		this.failure = escalation;

		// Send the response
		this.connectionHandler.sendResponse(this);
	}

	/**
	 * Loads the content of this HTTP response to the {@link WriteMessage} to
	 * send to the client.
	 *
	 * @param outputBufferStream
	 *            {@link OutputBufferStream}.
	 * @throws IOException
	 *             If fails to load the content.
	 */
	public synchronized void loadContent(
			final OutputBufferStream outputBufferStream) throws IOException {

		// Determine if failed to handle request
		if (this.failure != null) {

			// Clear the headers
			this.headers.clear();

			// Indicate failed processing
			this.status = HttpStatus._500;
			this.statusMessage = HttpStatus.getStatusMessage(this.status);

			// Write the exception as the body
			this.body.clear();
			ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
			this.failure.printStackTrace(new PrintStream(stackTrace));
			this.body.add(ByteBuffer.wrap(stackTrace.toByteArray()));
		}

		// Provide the content length
		int contentLength = 0;
		for (ByteBuffer buffer : this.body) {
			if (buffer.position() > 0) {
				// Make the buffer ready for writing
				buffer.flip();
			}

			// Increment the content by amount in buffer
			contentLength += buffer.limit();
		}
		this.headers.add(new Header(HEADER_NAME_CONTENT_LENGTH, String
				.valueOf(contentLength)));

		// Ensure appropriate successful status
		if ((contentLength == 0) && (this.status == HttpStatus._200)) {
			this.setStatus(HttpStatus._204);
		}

		// Create output stream to write content
		OutputStream outputStream = outputBufferStream.getOutputStream();

		// Provide US-ASCII translation of status line and response
		Writer writer = new OutputStreamWriter(outputStream, US_ASCII);

		// Write the status line
		writer.append(this.version);
		writer.append(' ');
		writer.append(String.valueOf(this.status));
		writer.append(' ');
		writer.append(this.statusMessage);
		writer.append(EOL);

		// Write the headers
		for (Header header : this.headers) {
			writer.append(header.name);
			writer.append(": ");
			writer.append(header.value);
			writer.append(EOL);
		}
		writer.append(EOL);

		// Header written
		writer.flush();

		// Write the body
		for (ByteBuffer buffer : this.body) {
			outputBufferStream.append(buffer);
		}

		// Body has been written
		this.body.clear();
	}

	/*
	 * ================ HttpResponse =======================================
	 */

	@Override
	public synchronized void setStatus(int status) {
		this.status = status;
		this.statusMessage = HttpStatus.getStatusMessage(status);
	}

	@Override
	public synchronized void setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	@Override
	public synchronized void setVersion(String version) {
		this.version = version;
	}

	@Override
	public synchronized void addHeader(String name, String value) {

		// Ignore specifying content length
		if (HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(name)) {
			return;
		}

		// Add the header
		this.headers.add(new Header(name, value));
	}

	@Override
	public synchronized OutputStream getBody() {
		return this.bodyOutputStream;
	}

	@Override
	public synchronized void appendToBody(ByteBuffer content) {
		// Append the content
		this.body.add(content);

		// No longer a current body buffer
		this.currentBodyBuffer = null;
	}

	/**
	 * <p>
	 * Obtains the current {@link ByteBuffer} to append body.
	 * <p>
	 * This works in conjunction with appending {@link ByteBuffer} instances.
	 *
	 * @return Current {@link ByteBuffer}.
	 */
	private ByteBuffer getCurrentBuffer() {

		// Ensure have current buffer that is not full
		if ((this.currentBodyBuffer == null)
				|| (this.currentBodyBuffer.remaining() == 0)) {
			this.currentBodyBuffer = ByteBuffer.allocate(this.connectionHandler
					.getResponseBufferLength());
			this.body.add(HttpResponseImpl.this.currentBodyBuffer);
		}

		// Return the current buffer to append data
		return HttpResponseImpl.this.currentBodyBuffer;
	}

	@Override
	public synchronized void send() throws IOException {

		// Do not send if already triggered to be sent
		if (this.isSent) {
			return;
		}

		// Send the response
		this.connectionHandler.sendResponse(this);
		this.isSent = true;
	}

	/**
	 * Header.
	 */
	private static class Header {

		/**
		 * Name.
		 */
		public final String name;

		/**
		 * Value.
		 */
		public final String value;

		/**
		 * Initiate.
		 *
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public Header(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * {@link OutputStream} for writing HTTP body.
	 */
	private class HttpOutputStream extends OutputStream {

		/*
		 * ================ OutputStream =============================
		 */

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (HttpResponseImpl.this) {

				// Obtain the current buffer
				ByteBuffer buffer = HttpResponseImpl.this.getCurrentBuffer();

				// Determine the length of bytes able to write
				int bufferLen = buffer.remaining() - 1; // as zero start
				int writeLen = Math.min(len, bufferLen);

				// Append the content
				buffer.put(b, off, writeLen);

				// Adjust length remaining to write
				len = len - writeLen;

				// Determine if further data to write
				if (len == 0) {
					return;
				}

				// Continue writing so adjust offset for next write
				off = off + writeLen;

				// Handle based on remaining size
				if (len < HttpResponseImpl.this.connectionHandler
						.getResponseBufferLength()) {
					// Write to new buffer (as only partially fills it)
					buffer = HttpResponseImpl.this.getCurrentBuffer();
					buffer.put(b, off, len);

				} else {
					// Large amount of data

					// Create copy in case writing from buffer
					byte[] data = new byte[len];
					System.arraycopy(b, off, data, 0, len);
					buffer = ByteBuffer.wrap(data);

					// Add buffer and flag to start on a new buffer
					HttpResponseImpl.this.body.add(buffer);
					HttpResponseImpl.this.currentBodyBuffer = null;
				}
			}
		}

		@Override
		public void write(int b) throws IOException {
			synchronized (HttpResponseImpl.this) {
				// Append to current buffer
				HttpResponseImpl.this.getCurrentBuffer().put((byte) b);
			}
		}
	}

}