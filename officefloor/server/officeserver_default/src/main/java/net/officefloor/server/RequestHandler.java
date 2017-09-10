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

import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Handles requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestHandler<R> {

	/**
	 * Handles a request.
	 * 
	 * @param request
	 *            Request.
	 */
	void handleRequest(R request);

	/**
	 * Sends data immediately.
	 * 
	 * @param immediateHead
	 *            Head {@link StreamBuffer} to linked list of
	 *            {@link StreamBuffer} instances of data to send immediately.
	 */
	void sendImmediateData(StreamBuffer<ByteBuffer> immediateHead);

	/**
	 * Allows to close connection.
	 */
	void closeConnection();

}