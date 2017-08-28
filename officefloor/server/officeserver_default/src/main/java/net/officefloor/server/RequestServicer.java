/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server;

/**
 * Services requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestServicer<R> {

	/**
	 * Services the request.
	 * 
	 * @param request
	 *            Request.
	 * @param responseWriter
	 *            {@link ResponseWriter}. To enable pipelining of requests, this
	 *            {@link ResponseWriter} must be invoked to indicate the request
	 *            has been serviced (even if no data to send).
	 */
	void service(R request, ResponseWriter responseWriter);

}