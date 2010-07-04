/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.value.loader;

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
	private final Object[] state;

	/**
	 * {@link StatelessValueLoader} to undertake loading the values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Object to load values on.
	 * @param state
	 *            State for loading values.
	 * @param delegate
	 *            {@link StatelessValueLoader} to undertake loading the values.
	 */
	public ValueLoaderImpl(Object object, Object[] state,
			StatelessValueLoader delegate) {
		this.object = object;
		this.state = state;
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoader ==========================
	 */

	@Override
	public void loadValue(String name, String value) throws Exception {
		// Load the value
		this.delegate.loadValue(this.object, name, value, this.state);
	}

}