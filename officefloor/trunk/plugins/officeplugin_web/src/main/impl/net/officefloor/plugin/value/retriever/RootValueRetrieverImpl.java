/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ValueRetriever} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RootValueRetrieverImpl<T> implements ValueRetriever<T> {

	/**
	 * Mapping of the property name to the {@link ValueRetriever}.
	 */
	private final Map<String, ValueRetriever<Object>> propertyToRetriever = new HashMap<String, ValueRetriever<Object>>();

	/**
	 * Indicates if case insensitive.
	 */
	private final boolean isCaseInsensitive;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link PropertyMetaData} instances.
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive.
	 */
	public RootValueRetrieverImpl(PropertyMetaData[] properties,
			boolean isCaseInsensitive) {
		this(properties, isCaseInsensitive,
				new HashMap<PropertyMetaData, ValueRetriever<?>>());
	}

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link PropertyMetaData} instances.
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive.
	 * @param valueRetrieverByMetaData
	 *            {@link ValueRetriever} by its {@link PropertyMetaData}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	RootValueRetrieverImpl(PropertyMetaData[] properties,
			boolean isCaseInsensitive,
			Map<PropertyMetaData, ValueRetriever<?>> valueRetrieverByMetaData) {
		this.isCaseInsensitive = isCaseInsensitive;

		// Load the property retriever
		for (PropertyMetaData property : properties) {

			// Obtain the property name
			String propertyName = property.getPropertyName();

			// Determine if already registered value retriever
			ValueRetriever propertyRetriever = valueRetrieverByMetaData
					.get(property);
			if (propertyRetriever == null) {
				// Create the property retriever (registers itself)
				propertyRetriever = new PropertyValueRetrieverImpl<Object>(
						property, this.isCaseInsensitive,
						valueRetrieverByMetaData);
			}

			// Register the property retriever
			this.propertyToRetriever.put(propertyName, propertyRetriever);
		}
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	/**
	 * {@link Processor} to determine if value is retrievable.
	 */
	private static Processor<Boolean> retrievable = new Processor<Boolean>() {
		@Override
		public Boolean process(ValueRetriever<Object> propertyRetriever,
				Object object, String remainingName) throws Exception {

			// Must be retriever to obtain value
			if (propertyRetriever == null) {
				return false; // property not retrievable
			}

			// Determine if further property
			if (remainingName.length() == 0) {
				return true; // property is retrievable
			}

			// Delegate to determine if retrievable
			return propertyRetriever.isValueRetrievable(remainingName);
		}
	};

	@Override
	public boolean isValueRetrievable(String name) throws Exception {
		// Return if value retrievable
		return this.process(name, null, retrievable).booleanValue();
	}

	/**
	 * {@link Processor} to retrieve the value.
	 */
	private static Processor<String> retriever = new Processor<String>() {
		@Override
		public String process(ValueRetriever<Object> propertyRetriever,
				Object object, String remainingName) throws Exception {

			// Ensure able to retrieve value
			if (propertyRetriever == null) {
				return null; // Unknown value
			}

			// Return the retrieved value
			return propertyRetriever.retrieveValue(object, remainingName);
		}
	};

	@Override
	public String retrieveValue(T object, String name) throws Exception {
		// Return the retrieved value
		return this.process(name, object, retriever);
	}

	/**
	 * Processes.
	 * 
	 * @param name
	 *            Property name to retrieve.
	 * @param object
	 *            Object on the value. May be <code>null</code>.
	 * @param processor
	 *            {@link Processor}.
	 * @return Value as per the {@link Processor}.
	 * @throws Exception
	 *             If fails to process.
	 */
	private <R> R process(String name, Object object, Processor<R> processor)
			throws Exception {

		// Obtain the property name
		String propertyName;
		String remainingName;
		int splitIndex = name.indexOf('.');
		if (splitIndex < 0) {
			propertyName = name;
			remainingName = "";
		} else {
			propertyName = name.substring(0, splitIndex);
			remainingName = name.substring(splitIndex + 1); // +1 ignore '.'
		}

		// Translate the property name
		if (this.isCaseInsensitive) {
			propertyName = propertyName.toLowerCase();
		}

		// Obtain the property value retriever
		ValueRetriever<Object> propertyRetriever = this.propertyToRetriever
				.get(propertyName);

		// Process
		return processor.process(propertyRetriever, object, remainingName);
	}

	/**
	 * Processor.
	 */
	private interface Processor<R> {

		/**
		 * Processes the details.
		 * 
		 * @param propertyRetriever
		 *            {@link ValueRetriever} for the property. May be
		 *            <code>null</code>.
		 * @param object
		 *            Object to retrieve value from.
		 * @param remainingName
		 *            Remaining name of the property.
		 * @return Value as per processing.
		 * @throws Exception
		 *             If fails to process.
		 */
		R process(ValueRetriever<Object> propertyRetriever, Object object,
				String remainingName) throws Exception;
	}

}