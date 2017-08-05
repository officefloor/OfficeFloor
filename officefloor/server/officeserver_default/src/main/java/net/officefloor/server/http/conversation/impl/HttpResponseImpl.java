/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.conversation.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.http.protocol.WriteBuffer;
import net.officefloor.server.http.protocol.WriteBufferEnum;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.ServerWriterImpl;
import net.officefloor.server.stream.WriteBufferReceiver;
import net.officefloor.server.stream.impl.DataWrittenException;
import net.officefloor.server.stream.impl.ServerOutputStreamImpl;

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
	 * Name of the header Server.
	 */
	private static final String HEADER_NAME_SERVER = "Server";

	/**
	 * Name of the header Date.
	 */
	private static final String HEADER_NAME_DATE = "Date";

	/**
	 * Name of the header Content-Type.
	 */
	private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";

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
	 * {@link HttpResponseWriteBufferReceiver}.
	 */
	private final HttpResponseWriteBufferReceiver receiver = new HttpResponseWriteBufferReceiver();

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion version;

	/**
	 * {@link HttpStatus}.
	 */
	private HttpStatus status;

	/**
	 * Headers.
	 */
	private final List<HttpHeader> headers = new LinkedList<HttpHeader>();

	/**
	 * {@link ServerOutputStream} containing the entity content.
	 */
	private final ServerOutputStreamImpl entity;

	/**
	 * Indicates if requested the {@link ServerOutputStream}. In other words,
	 * may not use {@link ServerWriter}.
	 */
	private boolean isOutputStream = false;

	/**
	 * Content-Type.
	 */
	private String contentType = null;

	/**
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset;

	/**
	 * Cache the {@link ServerWriter}. Also indicates if using the
	 * {@link ServerWriter}.
	 */
	private ServerWriter entityWriter = null;

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
	 *            {@link HttpVersion}.
	 */
	public HttpResponseImpl(HttpConversationImpl conversation, Connection connection, HttpVersion httpVersion) {
		this.conversation = conversation;
		this.connection = connection;

		// Specify initial values
		this.version = httpVersion;
		this.status = HttpStatus.OK;
		this.charset = this.conversation.getDefaultCharset();
		this.entity = new ServerOutputStreamImpl(this.receiver, this.conversation.getSendBufferSize());
	}

	/**
	 * Initiate.
	 * 
	 * @param conversation
	 *            {@link HttpConversationImpl}.
	 * @param connection
	 *            {@link Connection}.
	 * @param httpVersion
	 *            {@link HttpVersion}.
	 * @param momento
	 *            Momento containing the state for this {@link HttpResponse}.
	 */
	public HttpResponseImpl(HttpConversationImpl conversation, Connection connection, HttpVersion httpVersion,
			Serializable momento) {

		// Ensure valid momento
		if (!(momento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for " + HttpResponse.class.getSimpleName());
		}
		StateMomento state = (StateMomento) momento;

		// Load state
		this.conversation = conversation;
		this.connection = connection;
		this.version = httpVersion;
		this.status = HttpStatus.getHttpStatus(state.status);
		this.headers.addAll(state.headers);
		this.contentType = state.contentType;
		this.charset = Charset.forName(state.charset);
		this.entity = new ServerOutputStreamImpl(this.receiver, this.conversation.getSendBufferSize(),
				state.entityState);
		this.isOutputStream = state.isOutputStream;
		if (state.isEntityWriter) {
			this.loadEntityWriterThreadUnsafe();
		}
	}

	/**
	 * Exports the momento for the current state of this {@link HttpResponse}.
	 * 
	 * @return Momento for the current state of this {@link HttpResponse}.
	 * @throws DataWrittenException
	 *             Should data have already been written to the
	 *             {@link Connection}.
	 * @throws IOException
	 *             If fails to export state.
	 */
	Serializable exportState() throws DataWrittenException, IOException {

		synchronized (this.connection.getWriteLock()) {

			// Prepare state for momento
			List<HttpHeader> httpHeaders = new ArrayList<HttpHeader>(this.headers);
			String charsetSerializeName = this.charset.name();
			Serializable entityMomento = this.entity.exportState(this.entityWriter);

			// Create and return the state momento
			return new StateMomento(this.status.getStatusCode(), this.status.getStatusMessage(), httpHeaders,
					entityMomento, this.contentType, charsetSerializeName, this.isOutputStream,
					(this.entityWriter != null));
		}
	}

	/**
	 * <p>
	 * Queues the {@link HttpResponse} for sending if it is complete.
	 * 
	 * @return <code>true</code> should the {@link HttpResponse} be queued for
	 *         sending.
	 * @throws IOException
	 *             If fails writing {@link HttpResponse} if no need to queue.
	 */
	boolean queueHttpResponseIfComplete() throws IOException {
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
		synchronized (this.connection.getWriteLock()) {

			// Clear the response to write the failure
			this.resetUnsafe();

			// Send failure as plain text
			this.contentType = "text/plain";

			// Write the failure header details
			if (failure instanceof HttpRequestParseException) {
				// Parse request failure
				HttpRequestParseException parseFailure = (HttpRequestParseException) failure;
				this.setHttpStatus(parseFailure.getHttpStatus());
			} else {
				// Handling request failure
				this.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			}

			// Write the failure response
			ServerWriter writer = this.getEntityWriter();
			String failMessage = failure.getClass().getSimpleName() + ": " + failure.getMessage();
			writer.write(failMessage);
			if (this.conversation.isSendStackTraceOnFailure()) {
				// Provide the stack trace
				writer.write("\n\n");
				PrintWriter stackTraceWriter = new PrintWriter(writer);
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
	 * Handles possible {@link CleanupEscalation} instances.
	 * 
	 * @param cleanupEscalations
	 *            {@link CleanupEscalation} instances.
	 * @throws IOException
	 *             If fails to send the failure response.
	 */
	void handleCleanupEscalations(CleanupEscalation[] cleanupEscalations) throws IOException {

		// Determine if clean up escalations
		if ((cleanupEscalations == null) || (cleanupEscalations.length == 0)) {
			return; // no clean up escalations
		}

		// Lock as called from process completion handler
		synchronized (this.connection.getWriteLock()) {

			// Clear the response to write the failure
			this.resetUnsafe();

			// Send failure as plain text
			this.contentType = "text/plain";

			// Write the failure header details
			this.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);

			// Write the failure response
			ServerWriter writer = this.getEntityWriter();
			boolean isFirst = true;
			for (CleanupEscalation cleanupEscalation : cleanupEscalations) {
				Throwable escalation = cleanupEscalation.getEscalation();

				// Provide separator
				if (!isFirst) {
					writer.write("\n");
				}
				isFirst = false;

				// Write the escalation details to the response
				String failMessage = "Cleanup of object type " + cleanupEscalation.getObjectType().getName() + ": "
						+ escalation.getMessage() + " (" + escalation.getClass().getSimpleName() + ")";
				writer.write(failMessage);
				if (this.conversation.isSendStackTraceOnFailure()) {
					// Provide the stack trace
					writer.write("\n\n");
					PrintWriter stackTraceWriter = new PrintWriter(writer);
					escalation.printStackTrace(stackTraceWriter);
					stackTraceWriter.flush();
					writer.write("\n\n");
				}
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
		ServerOutputStream header = new ServerOutputStreamImpl(this.receiver, this.conversation.getSendBufferSize());

		// Ensure appropriate successful status for no content
		if ((contentLength == 0) && (this.status.isEqual(HttpStatus.OK))) {
			this.setHttpStatus(HttpStatus.NO_CONTENT);
		}

		// Write the status line
		writeUsAscii(this.version + " " + String.valueOf(this.status.getStatusCode()) + " "
				+ this.status.getStatusMessage() + EOL, header);

		// Write the managed headers
		writeUsAscii(HEADER_NAME_SERVER + ": " + this.conversation.getServerName() + EOL, header);
		if (false) {
			writeUsAscii(HEADER_NAME_DATE + ": " + this.conversation.getHttpServerClock().getDateHeaderValue() + EOL,
					header);
		}
		String contentType = this.getContentTypeThreadUnsafe();
		if (contentType != null) {
			writeUsAscii(HEADER_NAME_CONTENT_TYPE + ": " + contentType + EOL, header);
		}
		writeUsAscii(HEADER_NAME_CONTENT_LENGTH + ": " + contentLength + EOL, header);

		// Write the unmanaged headers
		for (HttpHeader httpHeader : this.headers) {
			String name = httpHeader.getName();
			String value = httpHeader.getValue();
			writeUsAscii(name + ": " + (value == null ? "" : value) + EOL, header);
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
	private static void writeUsAscii(String value, ServerOutputStream outputStream) throws IOException {
		outputStream.write(value.getBytes(HttpRequestParserImpl.US_ASCII));
	}

	/**
	 * Resets the {@link HttpResponse} without lock.
	 * 
	 * @throws IOException
	 *             If fails to reset.
	 */
	private void resetUnsafe() throws IOException {

		// Clear the response to write the failure
		this.headers.clear();
		this.entity.clear();
		this.receiver.entityBuffers.clear();
		this.contentType = null;
		this.charset = this.conversation.getDefaultCharset();
		this.isOutputStream = false;
		this.entityWriter = null;
	}

	/*
	 * ================ HttpResponse =======================================
	 */

	@Override
	public void setHttpVersion(HttpVersion version) {

		synchronized (this.connection.getWriteLock()) {

			// Specify the version
			this.version = version;
		}
	}

	@Override
	public HttpVersion getHttpVersion() {

		synchronized (this.connection.getWriteLock()) {

			// Return the version
			return this.version;
		}
	}

	@Override
	public void setHttpStatus(HttpStatus status) {

		synchronized (this.connection.getWriteLock()) {

			// Specify the status
			this.status = status;
		}
	}

	@Override
	public HttpStatus getHttpStatus() {

		synchronized (this.connection.getWriteLock()) {

			// Return the current status
			return this.status;
		}
	}

	@Override
	public void reset() throws IOException {

		synchronized (this.connection.getWriteLock()) {

			// Reset the response
			this.resetUnsafe();
		}
	}

	@Override
	public HttpHeader addHeader(String name, String value) {

		// Ignore specifying managed headers
		if (HEADER_NAME_SERVER.equalsIgnoreCase(name) || HEADER_NAME_DATE.equalsIgnoreCase(name)
				|| HEADER_NAME_CONTENT_TYPE.equalsIgnoreCase(name)
				|| HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(name)) {
			throw new IllegalArgumentException(HttpHeader.class.getSimpleName() + " '" + name
					+ "' can not be set, as is managed by the " + HttpResponse.class.getSimpleName());
		}

		// Create the HTTP header
		HttpHeader header = new HttpHeaderImpl(name, value);

		// Add the header
		synchronized (this.connection.getWriteLock()) {
			this.headers.add(header);
		}

		// Return the added header
		return header;
	}

	@Override
	public HttpHeader getHeader(String name) {

		synchronized (this.connection.getWriteLock()) {

			// Search for the first header by the name
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

		synchronized (this.connection.getWriteLock()) {

			// Create and return the array of headers
			return this.headers.toArray(new HttpHeader[0]);
		}
	}

	@Override
	public void removeHeader(HttpHeader header) {

		synchronized (this.connection.getWriteLock()) {

			// Remove the header
			this.headers.remove(header);
		}
	}

	@Override
	public void removeHeaders(String name) {

		synchronized (this.connection.getWriteLock()) {

			// Remove all headers by name
			for (Iterator<HttpHeader> iterator = this.headers.iterator(); iterator.hasNext();) {
				HttpHeader header = iterator.next();
				if (name.equalsIgnoreCase(header.getName())) {
					// Remove the header
					iterator.remove();
				}
			}
		}
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Ensure not using writer
			if (this.entityWriter != null) {
				throw new IOException("getEntityWriter() has already been invoked");
			}

			// Flag using the output stream
			this.isOutputStream = true;

			// Return the entity
			return this.entity;
		}
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Ensure not using entity writer
			if (this.entityWriter != null) {
				throw new IOException("getEntityWriter() has already been invoked");
			}

			// Specify the content type
			this.contentType = contentType;

			// Specify the charset (or use default if none provided)
			this.charset = (charset == null ? this.conversation.getDefaultCharset() : charset);
		}
	}

	@Override
	public String getContentType() {

		synchronized (this.receiver.getWriteLock()) {

			// Return the content type
			return this.getContentTypeThreadUnsafe();
		}
	}

	@Override
	public Charset getContentCharset() {

		synchronized (this.receiver.getWriteLock()) {

			// Return the charset
			return this.charset;
		}
	}

	/**
	 * Obtains the <code>Content-Type</code> header value.
	 * 
	 * @return <code>Content-Type</code> header value. May be <code>null</code>
	 *         if no <code>Content-Type</code> specified.
	 */
	private String getContentTypeThreadUnsafe() {

		// Determine if have content type
		if (this.contentType == null) {
			return null; // No content type
		}

		// Provide the content type (appending charset if using writer)
		return this.contentType + (this.entityWriter != null ? "; charset=" + this.charset.name() : "");
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Ensure not using output stream
			if (this.isOutputStream) {
				throw new IOException("getEntity() has already been invoked");
			}

			// Provide the default content type
			if (this.contentType == null) {
				this.contentType = "text/html";
			}

			// Lazy create the entity writer
			if (this.entityWriter == null) {
				this.loadEntityWriterThreadUnsafe();
			}

			// Return the entity writer
			return this.entityWriter;
		}
	}

	/**
	 * Loads the {@link ServerWriter}.
	 */
	private void loadEntityWriterThreadUnsafe() {
		this.entityWriter = new ServerWriterImpl(this.entity, this.charset, this.receiver.getWriteLock());
	}

	@Override
	public void send() throws IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Close the entity which triggers sending response
			if (this.entityWriter != null) {
				this.entityWriter.close();
			} else {
				this.entity.close();
			}
		}
	}

	/**
	 * {@link HttpResponse} {@link WriteBufferReceiver}.
	 */
	private class HttpResponseWriteBufferReceiver implements WriteBufferReceiver {

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
		 * @throws IOException
		 *             If fails writing {@link HttpResponse} if no need to
		 *             queue.
		 */
		public boolean queueHttpResponseIfComplete() throws IOException {

			// Ensure is closed (ie complete ready for sending)
			if (!this.isClosed()) {
				return false;
			}

			// Write the data for the response
			WriteBuffer[] responseData = new WriteBuffer[this.headerBuffers.length + this.entityBuffers.size()];
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
		 * 
		 * Thread safe as called within lock of ServerOutputStream
		 */

		@Override
		public Object getWriteLock() {
			return HttpResponseImpl.this.connection.getWriteLock();
		}

		@Override
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			return HttpResponseImpl.this.connection.createWriteBuffer(data, length);
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
					throw new IllegalStateException("Unknown " + WriteBuffer.class.getSimpleName() + " type: " + type);
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

	/**
	 * Momento for state of this {@link HttpResponse}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Status code.
		 */
		private final int status;

		/**
		 * Status message.
		 */
		private final String statusMessage;

		/**
		 * Headers.
		 */
		private final List<HttpHeader> headers;

		/**
		 * Momento containing the {@link ServerOutputStream} state.
		 */
		private final Serializable entityState;

		/**
		 * Content-Type.
		 */
		private final String contentType;

		/**
		 * {@link Charset} for the {@link ServerWriter}.
		 */
		private final String charset;

		/**
		 * Indicates if a {@link ServerOutputStream}.
		 */
		private final boolean isOutputStream;

		/**
		 * Indicates if a {@link ServerWriter}.
		 */
		private final boolean isEntityWriter;

		/**
		 * Initiate.
		 * 
		 * @param status
		 *            Status code.
		 * @param statusMessage
		 *            Status message.
		 * @param headers
		 *            Headers.
		 * @param entityState
		 *            Momento containing the {@link ServerOutputStream} state.
		 * @param contentType
		 *            Content-Type.
		 * @param charset
		 *            {@link Charset} for the {@link ServerWriter}.
		 * @param isOutputStream
		 *            Indicates if a {@link ServerOutputStream}.
		 * @param isEntityWriter
		 *            Indicates if a {@link ServerWriter}.
		 */
		public StateMomento(int status, String statusMessage, List<HttpHeader> headers, Serializable entityState,
				String contentType, String charset, boolean isOutputStream, boolean isEntityWriter) {
			this.status = status;
			this.statusMessage = statusMessage;
			this.headers = headers;
			this.entityState = entityState;
			this.contentType = contentType;
			this.charset = charset;
			this.isOutputStream = isOutputStream;
			this.isEntityWriter = isEntityWriter;
		}
	}

}