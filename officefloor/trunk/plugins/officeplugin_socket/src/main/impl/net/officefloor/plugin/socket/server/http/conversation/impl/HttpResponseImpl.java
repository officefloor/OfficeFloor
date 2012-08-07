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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.stream.WriteBufferReceiver;
import net.officefloor.plugin.stream.impl.ServerOutputStreamImpl;

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
	 * Send buffer size.
	 */
	private final int sendBufferSize;

	/**
	 * {@link HttpResponseWriteBufferReceiver}.
	 */
	private final HttpResponseWriteBufferReceiver receiver = new HttpResponseWriteBufferReceiver();

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
	 * {@link ServerOutputStream} containing the entity content.
	 */
	private final ServerOutputStreamImpl entity;

	/**
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param conversation
	 *            {@link HttpConversationImpl}.
	 * @param connection
	 *            {@link Connection}.
	 * @param httpVersion
	 *            HTTP version.
	 * @param sendBufferSize
	 *            Send buffer size.
	 */
	public HttpResponseImpl(HttpConversationImpl conversation,
			Connection connection, String httpVersion, int sendBufferSize) {
		this.conversation = conversation;
		this.connection = connection;
		this.sendBufferSize = sendBufferSize;

		// Specify initial values
		this.version = httpVersion;
		this.status = HttpStatus.SC_OK;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);
		this.entity = new ServerOutputStreamImpl(this.receiver,
				this.sendBufferSize);
	}

	/**
	 * <p>
	 * Queues the {@link HttpResponse} for sending if it is complete.
	 * 
	 * @return <code>true</code> should the {@link HttpResponse} be queued for
	 *         sending.
	 */
	boolean queueHttpResponseIfComplete() {
		return this.receiver.queueHttpResponseIfComplete();
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
			this.entity.clear();

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
			String failMessage = failure.getMessage();
			if (failMessage == null) {
				// No message so provide type of error
				failMessage = failure.getClass().getSimpleName();
			}
			this.entity.write(failMessage
					.getBytes(PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
			if (this.conversation.isSendStackTraceOnFailure()) {
				// Provide the stack trace
				this.entity.write("\n\n"
						.getBytes(PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
				PrintWriter stackTraceWriter = new PrintWriter(
						new OutputStreamWriter(this.entity,
								PARSE_FAILURE_CONTENT_ENCODING_CHARSET));
				failure.printStackTrace(stackTraceWriter);
				stackTraceWriter.flush();
			}

			// Send the response containing the failure
			this.send();

			// Close the connection
			this.connection.close();
		}
	}

	/**
	 * Writes the {@link HttpResponse} header to the {@link ServerOutputStream}.
	 * 
	 * @param contentLength
	 *            Content length of the entity.
	 * @throws IOException
	 *             If fails to load the content.
	 */
	private void writeHeader(long contentLength) throws IOException {

		// Create output stream to write header
		ServerOutputStream header = new ServerOutputStreamImpl(this.receiver,
				this.sendBufferSize);

		// Provide content length HTTP header
		this.headers.add(new HttpHeaderImpl(HEADER_NAME_CONTENT_LENGTH, String
				.valueOf(contentLength)));

		// Ensure appropriate successful status for no content
		if ((contentLength == 0) && (this.status == HttpStatus.SC_OK)) {
			this.setStatus(HttpStatus.SC_NO_CONTENT);
		}

		// Write the status line
		writeUsAscii(this.version + " " + String.valueOf(this.status) + " "
				+ this.statusMessage + EOL, header);

		// Write the headers
		for (HttpHeader httpHeader : this.headers) {
			String name = httpHeader.getName();
			String value = httpHeader.getValue();
			writeUsAscii(name + ": " + (value == null ? "" : value) + EOL,
					header);
		}
		writeUsAscii(EOL, header);

		// Flush the data
		header.flush();
	}

	/**
	 * Writes the value as US-ASCII to the {@link ServerOutputStream}.
	 * 
	 * @param value
	 *            Value.
	 * @param outputStream
	 *            {@link ServerOutputStream}.
	 * @throws IOException
	 *             If fails to write the value.
	 */
	private static void writeUsAscii(String value,
			ServerOutputStream outputStream) throws IOException {
		outputStream.write(value.getBytes(HttpRequestParserImpl.US_ASCII));
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
		this.setStatus(status, message);
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
	public ServerOutputStream getEntity() {
		return this.entity;
	}

	@Override
	public void setContentType(String contentType) {
		// TODO implement HttpResponse.setContentType
		throw new UnsupportedOperationException(
				"TODO implement HttpResponse.setContentType");
	}

	@Override
	public void setContentCharset(Charset charset) {
		// TODO implement HttpResponse.setContentCharset
		throw new UnsupportedOperationException(
				"TODO implement HttpResponse.setContentCharset");
	}

	@Override
	public void setContentCharset(String charset) {
		// TODO implement HttpResponse.setContentCharset
		throw new UnsupportedOperationException(
				"TODO implement HttpResponse.setContentCharset");
	}

	@Override
	public ServerWriter getEntityWriter() {
		// TODO implement HttpResponse.getEntityWriter
		throw new UnsupportedOperationException(
				"TODO implement HttpResponse.getEntityWriter");
	}

	@Override
	public void send() throws IOException {
		// Close the entity which triggers sending response
		this.getEntity().close();
	}

	/**
	 * {@link HttpResponse} {@link WriteBufferReceiver}.
	 */
	private class HttpResponseWriteBufferReceiver implements
			WriteBufferReceiver {

		/**
		 * HTTP header {@link WriteBuffer} instances.
		 */
		private WriteBuffer[] headerBuffers = null;

		/**
		 * Indicates if writing the HTTP header.
		 */
		private boolean isWritingHeader = false;

		/**
		 * Entity {@link WriteBuffer} instances.
		 */
		private final List<WriteBuffer> entityBuffers = new LinkedList<WriteBuffer>();

		/**
		 * Queues the {@link HttpResponse} for sending if complete.
		 * 
		 * @return <code>true</code> if queued for sending.
		 */
		public boolean queueHttpResponseIfComplete() {

			// Ensure is closed (ie complete ready for sending)
			if (!this.isClosed()) {
				return false;
			}

			// Write the data for the response
			WriteBuffer[] responseData = new WriteBuffer[this.headerBuffers.length
					+ this.entityBuffers.size()];
			int index = 0;
			for (WriteBuffer buffer : this.headerBuffers) {
				responseData[index++] = buffer;
			}
			for (WriteBuffer buffer : this.entityBuffers) {
				responseData[index++] = buffer;
			}

			// Queue the HTTP response for sending
			HttpResponseImpl.this.connection.writeData(responseData);

			// Queued for sending
			return true;
		}

		/*
		 * ==================== WriteBufferReceiver =================
		 */

		@Override
		public Object getLock() {
			return HttpResponseImpl.this.connection.getLock();
		}

		@Override
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			return HttpResponseImpl.this.connection.createWriteBuffer(data,
					length);
		}

		@Override
		public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
			return HttpResponseImpl.this.connection.createWriteBuffer(buffer);
		}

		@Override
		public void writeData(WriteBuffer[] data) {

			// Determine if writing the header
			if (this.isWritingHeader) {
				this.headerBuffers = data;

			} else {
				// Cache data of entity being written
				for (WriteBuffer buffer : data) {
					this.entityBuffers.add(buffer);
				}
			}
		}

		@Override
		public void close() throws IOException {

			// Note: all data should now be flushed

			// Calculate the content length
			long contentLength = 0;
			for (WriteBuffer buffer : this.entityBuffers) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {
				case BYTE_ARRAY:
					contentLength += buffer.length();
					break;

				case BYTE_BUFFER:
					contentLength += buffer.getDataBuffer().remaining();
					break;

				default:
					throw new IllegalStateException("Unknown "
							+ WriteBuffer.class.getSimpleName() + " type: "
							+ type);
				}
			}

			// Write the header
			this.isWritingHeader = true;
			HttpResponseImpl.this.writeHeader(contentLength);

			// Flag now closed
			HttpResponseImpl.this.isClosed = true;

			// Attempt to queue the HTTP response for sending
			HttpResponseImpl.this.conversation.queueCompleteResponses();
		}

		@Override
		public boolean isClosed() {
			return HttpResponseImpl.this.isClosed;
		}
	}

}