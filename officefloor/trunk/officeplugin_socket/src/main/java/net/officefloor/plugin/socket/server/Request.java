/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server;

import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Message identified by the {@link ConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public interface Request {

	/**
	 * Obtains the lock to <code>synchronize</code> for using this
	 * {@link Request}.
	 *
	 * @return Lock for this {@link Request}.
	 */
	Object getLock();

	/**
	 * Obtains the attachment provided by the {@link ConnectionHandler}.
	 *
	 * @return Attachment provided by the {@link ConnectionHandler}. May be
	 *         <code>null</code>.
	 */
	Object getAttachment();

	/**
	 * Obtains the {@link InputBufferStream} to obtain data from the client for
	 * this {@link Request}.
	 *
	 * @return {@link InputBufferStream}.
	 */
	InputBufferStream getInputBufferStream();

	/**
	 * Obtains the {@link OutputBufferStream} to write data to the client for
	 * this {@link Request}.
	 *
	 * @return {@link OutputBufferStream}.
	 */
	OutputBufferStream getOutputBufferStream();

}