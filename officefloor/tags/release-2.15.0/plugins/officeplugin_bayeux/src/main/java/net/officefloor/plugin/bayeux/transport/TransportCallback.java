/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.plugin.bayeux.transport;

/**
 * Generic transport callback.
 * 
 * @author Daniel Sagenschneider
 */
public interface TransportCallback<R> {

	/**
	 * Invoked for a successful result of the transport.
	 * 
	 * @param result
	 *            Result for the transport.
	 */
	void successful(R result);

	/**
	 * Invoked for an unsuccessful result of the transport.
	 * 
	 * @param result
	 *            Unsuccessful result of the transport.
	 */
	void unsuccessful(R result);

}