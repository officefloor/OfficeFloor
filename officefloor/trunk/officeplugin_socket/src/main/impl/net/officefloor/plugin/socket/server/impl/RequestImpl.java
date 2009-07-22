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
package net.officefloor.plugin.socket.server.impl;

import net.officefloor.plugin.socket.server.Request;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * {@link Request} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class RequestImpl implements Request {

	/**
	 * {@link InputBufferStream} to the {@link Request} content.
	 */
	private final InputBufferStream inputBufferStream;

	/**
	 * Attachment for the {@link Request}.
	 */
	private final Object attachment;

	/**
	 * Initiate.
	 *
	 * @param inputBufferStream
	 *            {@link InputBufferStream} to the {@link Request} content.
	 * @param attachment
	 *            Attachment for the {@link Request}.
	 */
	public RequestImpl(InputBufferStream inputBufferStream, Object attachment) {
		this.inputBufferStream = inputBufferStream;
		this.attachment = attachment;
	}

	/*
	 * ======================== Request ================================
	 */

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.inputBufferStream;
	}

	@Override
	public Object getAttachment() {
		return this.attachment;
	}

}