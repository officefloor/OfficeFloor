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
 * {@link ValueLoaderFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderFactoryImpl<T> implements ValueLoaderFactory<T> {

	/**
	 * Indicates the number of objects required within the state.
	 */
	private int numberOfObjectsInState;

	/**
	 * Delegate {@link StatelessValueLoader} to load values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param numberOfObjectsInState
	 *            Indicates the number of objects required within the state.
	 * @param delegate
	 *            Delegate {@link StatelessValueLoader} to load values.
	 */
	public ValueLoaderFactoryImpl(int numberOfObjectsInState,
			StatelessValueLoader delegate) {
		this.numberOfObjectsInState = numberOfObjectsInState;
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoaderFactory ============================
	 */

	@Override
	public ValueLoader createValueLoader(T object) throws Exception {
		// Create and return the new value loader
		return new ValueLoaderImpl(object,
				new Object[this.numberOfObjectsInState], this.delegate);
	}

}