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

/**
 * Configuration of how to process the request.
 * 
 * @author Daniel
 */
public class ProcessConfig {

	/**
	 * Flag indicating if to close connection on sending response.
	 */
	public boolean isClose = false;

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	/**
	 * Body to send back in the response message.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Message of exception to be thrown in processing.
	 */
	public String exception;

	public void setException(String exception) {
		this.exception = exception;
	}
}
