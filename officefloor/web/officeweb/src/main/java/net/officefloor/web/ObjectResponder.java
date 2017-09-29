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
package net.officefloor.web;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides ability to send an {@link Object} response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectResponder<T> {

	/**
	 * Obtains the object type expected for this {@link ObjectResponder}.
	 * 
	 * @return Type of object expected for this {@link ObjectResponder}.
	 */
	Class<T> getObjectType();

	/**
	 * Indicates whether can handle the {@link Object}.
	 * 
	 * @param object
	 *            Response {@link Object}.
	 * @return <code>true</code> if able to handle the object.
	 */
	default boolean isHandle(T object) {
		return this.getObjectType().isAssignableFrom(object.getClass());
	}

	/**
	 * Sends the object.
	 * 
	 * @param object
	 *            Object to send.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @throws IOException
	 *             If fails to send the object.
	 */
	void send(T object, ServerHttpConnection connection) throws IOException;

}