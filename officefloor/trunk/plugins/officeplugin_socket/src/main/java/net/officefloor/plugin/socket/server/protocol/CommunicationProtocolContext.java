/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.protocol;

import java.nio.ByteBuffer;

/**
 * Context for the {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CommunicationProtocolContext {

	/**
	 * <p>
	 * Obtains the size of the {@link ByteBuffer} instances for writing.
	 * {@link WriteBuffer} sizes for data should not exceed this value.
	 * <p>
	 * This allows the {@link CommunicationProtocolSource} to create buffers that
	 * match size to reduce the amount of copy routines required.
	 * 
	 * @return Size of the write buffers.
	 */
	int getWriteBufferSize();

}