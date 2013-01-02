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
package net.officefloor.plugin.socket.server.protocol;

/**
 * Context that is available to all handle methods for the
 * {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionHandlerContext {

	/**
	 * <p>
	 * Obtains the current time in milliseconds.
	 * <p>
	 * This should return similar to {@link System#currentTimeMillis()} but is
	 * provided to cache time for multiple quick operations that require only
	 * estimates of time (e.g. time out idle connections in seconds).
	 * <p>
	 * Note CPU operations should be in the nanoseconds.
	 * 
	 * @return Time measured in milliseconds.
	 */
	long getTime();

}