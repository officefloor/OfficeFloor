/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferSquirt;
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
	 * {@link Charset} name for {@link HttpRequestParseException}
	 * {@link HttpResponse}.
	 */
	private static final String PARSE_FAILURE_CONTENT_CHARSET_NAME = "UTF-8";

	/**
	 * <code>Content-Type</code> for the {@link HttpRequestParseException}
	 * {@link HttpResponse}.
	 */
	private static final String PARSE_FAILURE_CONTENT_TYPE = "text/html; charset="
			+ PARSE_FAILURE_CONTENT_CHARSET_NAME;

	/**
	 * {@link Charset} for {@link HttpRequestParseException}
	 * {@link HttpResponse}.
	 */
	private static final Charset PARSE_FAILURE_CONTENT_ENCODING_CHARSET = Charset
			.forName(PARSE_FAILURE_CONTENT_CHARSET_NAME);

	/**
	 * HTTP end of line sequence (CR, LF).
	 */
	private static final String EOL = "\r\n";

	/**
	 * Name of the header Content-Length.
	 */
	private static final String HEADER_NAME_CONTENT_LENGTH = "Content-Length";

	/**
	 * {@link HttpConversationImpl} that this {@link HttpResponse} is involved.
	 */
	private final HttpConversationImpl conversation;

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
	 * Flag indicating to close the {@link Connection} when this
	 * {@link HttpResponse} is sent.
	 */
	private final boolean isCloseConnectionAfterSending;

	/**
	 * Flags if this {@link HttpResponse} is complete.
	 */
	private boolean isComplete = false;

	/**
	 * Initiate by defaulting from the {@link HttpRequest}.
	 * 
	 * @param conversation
	 *            {@link HttpConversationImpl}.
	 * @param connection
	 *            {@link Connection}.
	 * @param squirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @param httpVersion
	 *            HTTP version.
	 * @param isCloseConnectionAfterSending
	 *            Flag indicating to close the {@link Connection} when this
	 *            {@link HttpResponse} is sent.
	 */
	public HttpResponseImpl(HttpConversationImpl conversation,
			Connection connection, BufferSquirtFactory squirtFactory,
			String httpVersion, boolean isCloseConnectionAfterSending) {
		this.conversation = conversation;
		this.connection = connection;
		this.isCloseConnectionAfterSending = isCloseConnectionAfterSending;

		// Create the body buffer
		this.body = new BufferStreamImpl(squirtFactory);
		this.safeOutputBufferStream = new SynchronizedOutputBufferStream(
				new ResponseOutputBufferStream(this.body
						.getOutputBufferStream()), this.connection.getLock());

		// Specify initial values
		this.version = httpVersion;
		this.status = HttpStatus.SC_OK;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);
	}

	/**
	 * Flags failure in processing the {@link HttpRequest}.
	 * 
	 * @param failure
	 *            Failure in processing the {@link HttpRequest}.
	 * @throws IOException
	 *             If fails to send the failure response.
	 */
	void sendFailure(Throwable failure) throws IOException {

		// Lock as called from Escalation Handler
		synchronized (this.connection.getLock()) {

			// Clear the response to write the failure
			this.headers.clear();
			InputBufferStream bodyInput = this.body.getInputBufferStream();
			bodyInput.skip(bodyInput.available());

			// Write the failure header details
			if (failure instanceof HttpRequestParseException) {
				// Parse request failure
				HttpRequestParseException parseFailure = (HttpRequestParseException) failure;
				this.setStatus(parseFailure.getHttpStatus());
			} else {
				// Handling request failure
				this.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
			this.addHeader("Content-Type", PARSE_FAILURE_CONTENT_TYPE);

			// Write the failure response
			OutputBufferStream body = this.getBody();
			String failMessage = failure.getMessage();
			if (failMessage == null) {
				// No message so provide type of error
				failMessage = failure.getClass().getSimpleName();
			}
			body.write(failMessage
					.getBytes(PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
			if (this.conversation.isSendStackTraceOnFailure()) {
				// Provide the stack trace
				body.write("\n\n"
						.getBytes(PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
				PrintWriter stackTraceWriter = new PrintWriter(
						new OutputStreamWriter(body.getOutputStream(),
								PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
				failure.printStackTrace(stackTraceWriter);
				stackTraceWriter.flush();
			}

			// Complete the response (triggers sending the failure)
			this.getBody().close();
		}
	}

	/**
	 * Sends the {@link HttpResponse} if it is completed.
	 * 
	 * @return <code>true</code> if {@link HttpResponse} is completed and was
	 *         added to the {@link Connection} output to be sent.
	 * @throws IOException
	 *             If fails to send the {@link HttpResponse}.
	 */
	boolean attemptSendResponse() throws IOException {

		// Determine if response complete to send
		if (this.isComplete) {
			// Send response and flag sent
			this.write();
			return true;
		}

		// Response not yet complete so not sent
		return false;
	}

	/**
	 * Flags the {@link HttpResponse} as complete and triggers sending.
	 * 
	 * @throws IOException
	 *             If fails to send complete {@link HttpResponse} instances.
	 */
	private void flagComplete() throws IOException {

		// Flag complete
		this.isComplete = true;

		// Trigger sending complete requests
		this.conversation.sendCompleteResponses();
	}

	/**
	 * Writes the {@link HttpResponse} to the {@link Connection}.
	 * 
	 * @throws IOException
	 *             If fails to load the content.
	 */
	private void write() throws IOException {
		
		// Obtain the output buffer stream
		OutputBufferStream output = this.connection.getOutputBufferStream();

		// Create temporary buffer
		byte[] tempBuffer = new byte[1];

		// Provide the content length
		long contentLength = this.body.available();
		if (contentLength < 0) {
			// Set to 0 if less than (as may be end of stream with no data)
			contentLength = 0;
		}
		this.headers.add(new HttpHeaderImpl(HEADER_NAME_CONTENT_LENGTH, String
				.valueOf(contentLength)));

		// Ensure appropriate successful status
		if ((contentLength == 0) && (this.status == HttpStatus.SC_OK)) {
			this.setStatus(HttpStatus.SC_NO_CONTENT);
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

		// Response sent, determine if now close the connection
		if (this.isCloseConnectionAfterSending) {
			// Close the connection as should be last response sent
			this.connection.getOutputBufferStream().close();
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

		// Do not write null text
		if (text == null) {
			return;
		}

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

		// Obtain the status message
		String message = HttpStatus.getStatusMessage(status);

		// Set status and message
		synchronized (this.connection.getLock()) {
			this.status = status;
			this.statusMessage = message;
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
	public HttpHeader addHeader(String name, String value) {

		// Create the HTTP header
		HttpHeader header = new HttpHeaderImpl(name, value);

		// Ignore specifying content length
		if (HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(name)) {
			return header;
		}

		// Add the header
		synchronized (this.connection.getLock()) {
			this.headers.add(header);
		}

		// Return the added header
		return header;
	}

	@Override
	public HttpHeader getHeader(String name) {

		// Search for the first header by the name
		synchronized (this.connection.getLock()) {
			for (HttpHeader header : this.headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					// Found first header so return it
					return header;
				}
			}
		}

		// As here did not find header by name
		return null;
	}

	@Override
	public HttpHeader[] getHeaders() {

		// Create the array of headers
		HttpHeader[] headers;
		synchronized (this.connection.getLock()) {
			headers = this.headers.toArray(new HttpHeader[0]);
		}

		// Return the headers
		return headers;
	}

	@Override
	public void removeHeader(HttpHeader header) {
		// Remove the header
		synchronized (this.connection.getLock()) {
			this.headers.remove(header);
		}
	}

	@Override
	public void removeHeaders(String name) {
		// Remove all headers by name
		synchronized (this.connection.getLock()) {
			for (Iterator<HttpHeader> iterator = this.headers.iterator(); iterator
					.hasNext();) {
				HttpHeader header = iterator.next();
				if (name.equalsIgnoreCase(header.getName())) {
					// Remove the header
					iterator.remove();
				}
			}
		}
	}

	@Override
	public OutputBufferStream getBody() {
		return this.safeOutputBufferStream;
	}

	@Override
	public void send() throws IOException {
		// Close the body which triggers sending response
		this.getBody().close();
	}

	/**
	 * Response {@link OutputBufferStream}.
	 */
	private class ResponseOutputBufferStream implements OutputBufferStream {

		/**
		 * Backing {@link OutputBufferStream}.
		 */
		private final OutputBufferStream backingStream;

		/**
		 * Initiate.
		 * 
		 * @param backingStream
		 *            Backing {@link OutputBufferStream}.
		 */
		public ResponseOutputBufferStream(OutputBufferStream backingStream) {
			this.backingStream = backingStream;
		}

		/*
		 * ================ OutputBufferStream ============================
		 */

		@Override
		public OutputStream getOutputStream() {
			return new ResponseOutputStream(this.backingStream
					.getOutputStream());
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			this.backingStream.write(bytes);
		}

		@Override
		public void write(byte[] data, int offset, int length)
				throws IOException {
			this.backingStream.write(data, offset, length);
		}

		@Override
		public void write(BufferPopulator populator) throws IOException {
			this.backingStream.write(populator);
		}

		@Override
		public void append(ByteBuffer buffer) throws IOException {
			this.backingStream.append(buffer);
		}

		@Override
		public void append(BufferSquirt squirt) throws IOException {
			this.backingStream.append(squirt);
		}

		@Override
		public void close() throws IOException {
			this.backingStream.close();

			// Flag response complete
			HttpResponseImpl.this.flagComplete();
		}
	}

	/**
	 * Response {@link OutputStream}.
	 */
	private class ResponseOutputStream extends OutputStream {

		/**
		 * Backing {@link OutputStream}.
		 */
		private final OutputStream backingStream;

		/**
		 * Initiate.
		 * 
		 * @param backingStream
		 *            Backing {@link OutputStream}.
		 */
		public ResponseOutputStream(OutputStream backingStream) {
			this.backingStream = backingStream;
		}

		/*
		 * =================== OutputStream =========================
		 */

		@Override
		public void write(int b) throws IOException {
			this.backingStream.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.backingStream.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.backingStream.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.backingStream.flush();
		}

		@Override
		public void close() throws IOException {
			this.backingStream.close();

			// Flag response complete
			HttpResponseImpl.this.flagComplete();
		}
	}

}