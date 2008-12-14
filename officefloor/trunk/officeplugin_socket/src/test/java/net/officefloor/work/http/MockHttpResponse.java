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
package net.officefloor.work.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import junit.framework.TestCase;

import net.officefloor.plugin.socket.server.http.api.HttpResponse;

/**
 * Mock {@link HttpResponse}.
 * 
 * @author Daniel
 */
public class MockHttpResponse implements HttpResponse {

	/**
	 * Status.
	 */
	private int status = -1;

	/**
	 * Status message.
	 */
	private String statusMessage = null;

	/**
	 * Version.
	 */
	private String version = null;

	/**
	 * Headers.
	 */
	private Properties headers = new Properties();

	/**
	 * Body.
	 */
	private ByteArrayOutputStream body = new ByteArrayOutputStream();

	/**
	 * Flag indicating if sent.
	 */
	private boolean isSent = false;

	/**
	 * Obtains the HTTP status.
	 * 
	 * @return HTTP status.
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Obtains the HTTP status message.
	 * 
	 * @return HTTP status message.
	 */
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * Obtains the HTTP version.
	 * 
	 * @return HTTP version.
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Obtains the header value.
	 * 
	 * @param name
	 *            Header name.
	 * @return Corresponding header value.
	 */
	public String getHeaderValue(String name) {
		return this.headers.getProperty(name);
	}

	/**
	 * Obtains the body content.
	 * 
	 * @return Body content.
	 */
	public byte[] getBodyContent() {
		return this.body.toByteArray();
	}

	/**
	 * Flags if this {@link HttpResponse} is sent.
	 * 
	 * @return <code>true</code> if this {@link HttpResponse} is sent.
	 */
	public boolean isSent() {
		return this.isSent;
	}

	/*
	 * =============== HttpResponse =========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse#setStatus(int)
	 */
	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse#setStatus(int,
	 * java.lang.String)
	 */
	@Override
	public void setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse#setVersion
	 * (java.lang.String)
	 */
	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse#addHeader(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String name, String value) {
		headers.setProperty(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse#appendToBody
	 * (java.nio.ByteBuffer)
	 */
	@Override
	public void appendToBody(ByteBuffer content) {
		try {
			// Obtain the content
			ByteBuffer buffer = content.duplicate();
			if (buffer.position() > 0) {
				buffer.flip();
			}
			byte[] data = new byte[buffer.limit()];
			buffer.get(data);

			// Write the content to body
			this.body.write(data);
			this.body.flush();

		} catch (IOException ex) {
			// Should not have failure
			TestCase.fail("Failed to appendToBody by mock: " + ex.getMessage()
					+ " [" + ex.getClass().getSimpleName() + "]");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#getBody()
	 */
	@Override
	public OutputStream getBody() {
		return this.body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.http.api.HttpResponse#send()
	 */
	@Override
	public void send() throws IOException {
		this.isSent = true;
	}

}
