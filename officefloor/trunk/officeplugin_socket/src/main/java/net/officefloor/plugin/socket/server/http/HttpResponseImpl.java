/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;

/**
 * {@link HttpResponse}.
 * 
 * @author Daniel
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
	 * Body.
	 */
	private final ByteArrayOutputStream body = new ByteArrayOutputStream();

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
	 * Obtains the content of this HTTP response to send to the client.
	 * 
	 * @return Content of this HTTP response to send to the client.
	 * @throws IOException
	 *             If fails to obtain the content.
	 */
	public synchronized byte[] getContent() throws IOException {

		// Determine if failed to handle request
		if (this.failure != null) {

			// Clear the headers
			this.headers.clear();

			// Indicate failed processing
			this.status = HttpStatus._500;
			this.statusMessage = HttpStatus.getStatusMessage(this.status);

			// Write the exception as the body
			this.body.reset();
			this.failure.printStackTrace(new PrintStream(this.body));
		}

		// Provide the content length
		int contentLength = this.body.size();
		this.headers.add(new Header("Content-Length", String
				.valueOf(contentLength)));

		// Ensure appropriate successful status
		if ((contentLength == 0) && (this.status == 200)) {
			this.setStatus(204);
		}

		// Create the buffer for the response
		ByteArrayOutputStream content = new ByteArrayOutputStream(this.body
				.size()
				+ (this.headers.size() * 40));

		// Provide US-ASCII translation of status line and response
		Writer writer = new OutputStreamWriter(content, US_ASCII);

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
		this.body.writeTo(content);

		// Return the content
		return content.toByteArray();
	}

	/*
	 * =============================================================================
	 * HttpResponse
	 * =============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#setStatus(int)
	 */
	@Override
	public synchronized void setStatus(int status) {
		this.status = status;
		this.statusMessage = HttpStatus.getStatusMessage(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#setStatus(int,
	 *      java.lang.String)
	 */
	@Override
	public synchronized void setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#setVersion(java.lang.String)
	 */
	@Override
	public synchronized void setVersion(String version) {
		this.version = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#addHeader(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public synchronized void addHeader(String name, String value) {
		this.headers.add(new Header(name, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#getBody()
	 */
	@Override
	public synchronized OutputStream getBody() {
		return this.body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#send()
	 */
	@Override
	public synchronized void send() throws IOException {
		// Send the response
		this.connectionHandler.sendResponse(this);
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

}
