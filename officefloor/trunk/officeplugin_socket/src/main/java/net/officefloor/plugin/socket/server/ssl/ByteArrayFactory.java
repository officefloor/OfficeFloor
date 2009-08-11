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
package net.officefloor.plugin.socket.server.ssl;


/**
 * Creates a <code>byte array</code>.
 *
 * @author Daniel Sagenschneider
 */
public interface ByteArrayFactory {

	/**
	 * The general use of the created <code>byte array</code> is for temporary
	 * use and the returned <code>byte array</code> will be at least the size
	 * specified (it may however be bigger).
	 *
	 * @param minimumSize
	 *            Minimum size for the returned <code>byte array</code>.
	 * @return byte array.
	 */
	byte[] createByteArray(int minimumSize);

}