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
package net.officefloor.web.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectResponder;
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
	 */
	void loadValues(ValueLoader valueLoader);

	/**
	 * Obtains the {@link HttpObjectParser} instances.
	 * 
	 * @return {@link HttpObjectParser} instances. May be <code>null</code> if
	 *         no {@link HttpObjectParser} instances.
	 */
	HttpObjectParser<?>[] getObjectParsers();

	/**
	 * Obtains the {@link HttpObjectResponder} instances.
	 * 
	 * @return {@link HttpObjectResponder} instances. May be <code>null</code>
	 *         if no {@link HttpObjectResponder} instances.
	 */
	HttpObjectResponder<?>[] getObjectResponders();

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

	/**
	 * Exports a momento for the current state of this {@link HttpRequestState}.
	 * 
	 * @return Momento for the current state of this {@link HttpRequestState}.
	 * @throws IOException
	 *             If fails to export state.
	 */
	Serializable exportState() throws IOException;

	/**
	 * Imports the state from the momento.
	 * 
	 * @param momento
	 *            Momento containing the state for the {@link HttpRequestState}.
	 * @throws IOException
	 *             If fails to import state.
	 * @throws IllegalArgumentException
	 *             If invalid momento.
	 */
	void importState(Serializable momento) throws IOException, IllegalArgumentException;

}