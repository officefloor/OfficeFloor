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

import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Services the {@link Socket}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SocketServicer<R> {

	/**
	 * Services the {@link Socket}.
	 * 
	 * @param readBuffer
	 *            {@link StreamBuffer} containing the just read bytes. Note that
	 *            this could be the same {@link StreamBuffer} as previous, with
	 *            just further bytes written.
	 * @param requestHandler
	 *            Services the requests from the {@link Socket}.
	 */
	void service(StreamBuffer<ByteBuffer> readBuffer, RequestHandler<R> requestHandler);

	/**
	 * Enables translating responses.
	 * 
	 * @param headResponseBuffer
	 *            Head {@link StreamBuffer} to the linked list of
	 *            {@link StreamBuffer} instances for the response.
	 * @param responseHandler
	 *            {@link ResponseHandler}.
	 */
	default void translateResponse(StreamBuffer<ByteBuffer> headResponseBuffer, ResponseHandler responseHandler) {
		responseHandler.sendResponse(headResponseBuffer);
	}

}