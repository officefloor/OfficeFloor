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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ValueLoaderFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderFactoryImpl<T> implements ValueLoaderFactory<T> {

	/**
	 * Delegate {@link StatelessValueLoader} to load values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate Delegate {@link StatelessValueLoader} to load values.
	 */
	public ValueLoaderFactoryImpl(StatelessValueLoader delegate) {
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoaderFactory ============================
	 */

	@Override
	public ValueLoader createValueLoader(T object) throws Exception {
		// Create and return the new value loader
		return new ValueLoaderImpl(object, this.delegate);
	}

	@Override
	public ValueName[] getValueNames() {

		// Load the values
		List<ValueName> valueNames = new ArrayList<>();
		this.delegate.visitValueNames((name) -> valueNames.add(name), null, null);

		// Return the value names
		return valueNames.toArray(new ValueName[valueNames.size()]);
	}

}
