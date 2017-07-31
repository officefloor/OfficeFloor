/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Context for the {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CommunicationProtocolContext {

	/**
	 * <p>
	 * Obtains the size of the {@link ByteBuffer} instances for writing. The
	 * {@link WriteBuffer} sizes for data should not exceed this value.
	 * <p>
	 * This allows the {@link CommunicationProtocolSource} to create buffers
	 * that match size to reduce the amount of copy routines required.
	 * 
	 * @return Size of the write buffers for sending data.
	 */
	int getSendBufferSize();

	/**
	 * Obtains the default {@link Charset} for the server.
	 * 
	 * @return Default {@link Charset} for the server.
	 */
	Charset getDefaultCharset();

}