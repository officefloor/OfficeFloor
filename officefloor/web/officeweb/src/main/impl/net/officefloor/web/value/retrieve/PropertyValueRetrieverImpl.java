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

package net.officefloor.web.value.retrieve;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.server.http.HttpException;

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
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive.
	 * @param valueRetrieverByMetaData
	 *            {@link ValueRetriever} by its {@link PropertyMetaData}.
	 */
	PropertyValueRetrieverImpl(PropertyMetaData metaData, boolean isCaseInsensitive,
			Map<PropertyMetaData, ValueRetriever<?>> valueRetrieverByMetaData) {
		this.metaData = metaData;

		// Register this property before continuing further.
		// Must be registered before recursively creating further retrievers.
		valueRetrieverByMetaData.put(metaData, this);

		// Create further retrievers in the property meta-data tree
		this.delegate = new RootValueRetrieverImpl<Object>(this.metaData.getProperties(), isCaseInsensitive,
				valueRetrieverByMetaData);
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	@Override
	public Class<?> getValueType(String name) throws HttpException {
		// Delegate to obtain value type
		return this.delegate.getValueType(name);
	}

	@Override
	public <A> A getValueAnnotation(String name, Class<A> annotationType) throws HttpException {
		// Delegate to obtain value annotation
		return this.delegate.getValueAnnotation(name, annotationType);
	}

	@Override
	public Object retrieveValue(T object, String name) throws HttpException {

		// Ensure have value
		if (object == null) {
			return null; // no value
		}

		try {
			// Obtain the method
			Method method = this.metaData.getMethod(object.getClass());

			// Retrieve the property value
			Object value = ValueRetrieverSource.retrieveValue(object, method);

			// Determine if further property navigation
			if (name.length() > 0) {
				// Further navigation
				return this.delegate.retrieveValue(value, name);
			} else {
				// Navigated to the value, so return the value
				return value;
			}

		} catch (Exception ex) {
			throw new RetrieveValueException(ex);
		}
	}

}
