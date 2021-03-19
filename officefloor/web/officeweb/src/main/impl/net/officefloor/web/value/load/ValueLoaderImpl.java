/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.value.load;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * {@link ValueLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderImpl implements ValueLoader {

	/**
	 * Object to load values on.
	 */
	private final Object object;

	/**
	 * State for loading values.
	 */
	private final Map<PropertyKey, Object> state = new HashMap<PropertyKey, Object>();

	/**
	 * {@link StatelessValueLoader} to undertake loading the values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Object to load values on.
	 * @param delegate
	 *            {@link StatelessValueLoader} to undertake loading the values.
	 */
	public ValueLoaderImpl(Object object, StatelessValueLoader delegate) {
		this.object = object;
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoader ==========================
	 */

	@Override
	public void loadValue(String name, String value, HttpValueLocation location) throws HttpException {
		// Load the value
		this.delegate.loadValue(this.object, name, 0, value, location, this.state);
	}

}
