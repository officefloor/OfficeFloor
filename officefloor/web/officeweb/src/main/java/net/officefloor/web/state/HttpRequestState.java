/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.state;

import java.io.Serializable;
import java.util.Iterator;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.value.load.ValueLoader;

/**
 * State for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestState {

	/**
	 * Loads values from the {@link HttpRequest}.
	 * 
	 * @param valueLoader
	 *            {@link ValueLoader} to receive the values.
	 * @throws HttpException
	 *             if fails to load values.
	 */
	void loadValues(ValueLoader valueLoader) throws HttpException;

	/**
	 * Obtains the {@link Object} that is bound to the name.
	 * 
	 * @param name
	 *            Name.
	 * @return {@link Object} bound to the name or <code>null</code> if no
	 *         {@link Object} bound by the name.
	 */
	Serializable getAttribute(String name);

	/**
	 * Obtains an {@link Iterator} to the names of the bound {@link Object}
	 * instances.
	 * 
	 * @return {@link Iterator} to the names of the bound {@link Object}
	 *         instances.
	 */
	Iterator<String> getAttributeNames();

	/**
	 * Binds the {@link Object} to the name.
	 * 
	 * @param name
	 *            Name.
	 * @param object
	 *            {@link Object}. Must be {@link Serializable} as this
	 *            {@link HttpRequestState} may be stored in the
	 *            {@link HttpSession} to maintain its state across a redirect.
	 */
	void setAttribute(String name, Serializable object);

	/**
	 * Removes the bound {@link Object} by the name.
	 * 
	 * @param name
	 *            Name of bound {@link Object} to remove.
	 */
	void removeAttribute(String name);

}