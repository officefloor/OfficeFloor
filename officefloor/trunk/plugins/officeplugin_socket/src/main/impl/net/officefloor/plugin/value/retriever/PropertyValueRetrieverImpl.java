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
package net.officefloor.plugin.value.retriever;

import java.lang.reflect.Method;

import net.officefloor.plugin.value.loader.NameTranslator;

/**
 * {@link ValueRetriever} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyValueRetrieverImpl<T> implements ValueRetriever<T> {

	/**
	 * {@link PropertyMetaData}.
	 */
	private final PropertyMetaData metaData;

	/**
	 * Delegate {@link ValueRetriever}.
	 */
	private final ValueRetriever<Object> delegate;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link PropertyMetaData}.
	 * @param translator
	 *            {@link NameTranslator}.
	 */
	public PropertyValueRetrieverImpl(PropertyMetaData metaData,
			NameTranslator translator) {
		this.metaData = metaData;
		this.delegate = new RootValueRetrieverImpl<Object>(this.metaData
				.getProperties(), translator);
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	@Override
	public String retrieveValue(T object, String name) throws Exception {

		// Ensure have value
		if (object == null) {
			return null; // no value
		}

		// Obtain the method
		Method method = this.metaData.getMethod(object.getClass());

		// Retrieve the property value
		Object value = ValueRetrieverSourceImpl.retrieveValue(object, method);

		// Determine if further property navigation
		if (name.length() > 0) {
			// Further navigation
			return this.delegate.retrieveValue(value, name);
		} else {
			// Navigated to the value, so return the value
			return (value == null ? null : value.toString());
		}
	}

}