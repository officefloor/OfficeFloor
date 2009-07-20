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
package net.officefloor.plugin.stream;

import java.nio.ByteBuffer;

/**
 * <p>
 * Wraps the {@link Exception} of the {@link BufferProcessor} in failing to
 * process the {@link ByteBuffer}.
 * <p>
 * The {@link BufferProcessor} {@link Exception} can be obtained from
 * {@link #getCause()}.
 *
 * @author Daniel Sagenschneider
 */
public class BufferProcessException extends Exception {

	/**
	 * Initiate.
	 *
	 * @param cause
	 *            {@link BufferProcessor} {@link Exception}.
	 */
	public BufferProcessException(Throwable cause) {
		super(cause);
	}

}