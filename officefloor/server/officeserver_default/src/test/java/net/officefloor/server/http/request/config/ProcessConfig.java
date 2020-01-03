/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.request.config;

/**
 * Configuration of how to process the request.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessConfig {

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
