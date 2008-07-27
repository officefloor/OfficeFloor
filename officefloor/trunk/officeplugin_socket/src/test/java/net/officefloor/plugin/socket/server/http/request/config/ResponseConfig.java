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
package net.officefloor.plugin.socket.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the expected HTTP response.
 * 
 * @author Daniel
 */
public class ResponseConfig {

	/**
	 * Status.
	 */
	public int status = 200;

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Message.
	 */
	public String message = "OK";

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Version.
	 */
	public String version = "HTTP/1.1";

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Headers.
	 */
	public final List<HeaderConfig> headers = new LinkedList<HeaderConfig>();

	public void addHeader(HeaderConfig header) {
		this.headers.add(header);
	}

	/**
	 * Body.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Flag indicating if the connection should have been closed.
	 */
	public boolean isClosed = false;

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
