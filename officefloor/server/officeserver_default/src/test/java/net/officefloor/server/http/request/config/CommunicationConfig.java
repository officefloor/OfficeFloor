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
 * Configuration of a request/response communication.
 * 
 * @author Daniel Sagenschneider
 */
public class CommunicationConfig {

	/**
	 * {@link RequestConfig}.
	 */
	public RequestConfig request;

	public void setRequest(RequestConfig request) {
		this.request = request;
	}

	/**
	 * {@link ProcessConfig}.
	 */
	public ProcessConfig process;

	public void setProcess(ProcessConfig process) {
		this.process = process;
	}

	/**
	 * {@link ResponseConfig}.
	 */
	public ResponseConfig response;

	public void setResponse(ResponseConfig response) {
		this.response = response;
	}
}
