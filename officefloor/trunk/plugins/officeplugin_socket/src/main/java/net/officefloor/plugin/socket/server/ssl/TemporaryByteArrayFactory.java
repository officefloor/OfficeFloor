/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
 * Creates temporary <code>byte array</code> instances necessary for the
 * {@link SslConnection} to decipher data.
 *
 * @author Daniel Sagenschneider
 */
public interface TemporaryByteArrayFactory {

	/**
	 * <p>
	 * Creates a <code>byte array</code> that will be at least the size
	 * specified (it may however be bigger to allow reuse of
	 * <code>byte array</code> instances).
	 * <p>
	 * This will always be a different <code>byte array</code> to that returned
	 * from other methods of this interface.
	 *
	 * @param minimumSize
	 *            Minimum size for the returned <code>byte array</code>.
	 * @return Temporary <code>byte array</code>.
	 */
	byte[] createSourceByteArray(int minimumSize);

	/**
	 * <p>
	 * Creates a <code>byte array</code> that will be at least the size
	 * specified (it may however be bigger to allow reuse of
	 * <code>byte array</code> instances).
	 * <p>
	 * This will always be a different <code>byte array</code> to that returned
	 * from other methods of this interface.
	 *
	 * @param minimumSize
	 *            Minimum size for the returned <code>byte array</code>.
	 * @return Temporary <code>byte array</code>.
	 */
	byte[] createDestinationByteArray(int minimumSize);

}