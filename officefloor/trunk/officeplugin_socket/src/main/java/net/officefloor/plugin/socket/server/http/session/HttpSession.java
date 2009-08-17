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
package net.officefloor.plugin.socket.server.http.session;

import java.util.Iterator;

/**
 * HTTP session.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSession {

	/**
	 * Obtains the session Id.
	 *
	 * @return Session Id.
	 */
	String getSessionId();

	/**
	 * Indicates if this is a new {@link HttpSession}.
	 *
	 * @return <code>true</code> if this is a new {@link HttpSession}.
	 */
	boolean isNew();

	/**
	 * Obtains the time this {@link HttpSession} was created.
	 *
	 * @return Time this {@link HttpSession} was created.
	 */
	long getCreationTime();

	/**
	 * Invalidates this {@link HttpSession}.
	 */
	void invalidate();

	/**
	 * Obtains the {@link Object} that is bound to the name for this
	 * {@link HttpSession}.
	 *
	 * @param name
	 *            Name.
	 * @return {@link Object} bound to the name or <code>null</code> if no
	 *         {@link Object} bound by the name.
	 */
	Object getAttribute(String name);

	/**
	 * Obtains an {@link Iterator} to the names of the bound {@link Object}
	 * instances.
	 *
	 * @return {@link Iterator} to the names of the bound {@link Object}
	 *         instances.
	 */
	Iterator<String> getAttributeNames();

	/**
	 * Binds the {@link Object} to the name within this {@link HttpSession}.
	 *
	 * @param name
	 *            Name.
	 * @param object
	 *            {@link Object}.
	 */
	void setAttribute(String name, Object object);

	/**
	 * Removes the bound {@link Object} by the name from this
	 * {@link HttpSession}.
	 *
	 * @param name
	 *            Name of bound {@link Object} to remove.
	 */
	void removeAttribute(String name);

}