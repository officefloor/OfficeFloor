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
package net.officefloor.plugin.value.retriever;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ValueRetriever} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RootValueRetrieverImpl<T> implements ValueRetriever<T> {

	/**
	 * Mapping of the property name to the {@link RetrieverStruct}.
	 */
	private final Map<String, RetrieveStruct> propertyToRetriever = new HashMap<String, RetrieveStruct>();

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
			this.propertyToRetriever.put(propertyName, new RetrieveStruct(
					propertyRetriever, property));
		}
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	/**
	 * {@link Processor} to determine if value is retrievable.
	 */
	private static Processor<Method> retrievable = new Processor<Method>() {
		@Override
		public Method process(RetrieveStruct propertyRetriever, Object object,
				String remainingName) throws Exception {

			// Must be retriever to obtain value
			if (propertyRetriever == null) {
				return null; // property not retrievable
			}

			// Determine if further property
			if (remainingName.length() == 0) {
				// No further properties, so provide type method
				return propertyRetriever.metaData.getTypeMethod();
			}

			// Delegate to obtain type method
			return propertyRetriever.retriever.getTypeMethod(remainingName);
		}
	};

	@Override
	public Method getTypeMethod(String name) throws Exception {
		// Return type method
		return this.process(name, null, retrievable);
	}

	/**
	 * {@link Processor} to retrieve the value.
	 */
	private static Processor<Object> retriever = new Processor<Object>() {
		@Override
		public Object process(RetrieveStruct propertyRetriever, Object object,
				String remainingName) throws Exception {

			// Ensure able to retrieve value
			if (propertyRetriever == null) {
				return null; // Unknown value
			}

			// Return the retrieved value
			return propertyRetriever.retriever.retrieveValue(object,
					remainingName);
		}
	};

	@Override
	public Object retrieveValue(T object, String name) throws Exception {
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

		// Obtain the property value retrieve struct
		RetrieveStruct retrieveStruct = this.propertyToRetriever
				.get(propertyName);

		// Process
		return processor.process(retrieveStruct, object, remainingName);
	}

	/**
	 * Processor.
	 */
	private interface Processor<R> {

		/**
		 * Processes the details.
		 * 
		 * @param retrieveStruct
		 *            {@link RetrieveStruct} for the property. May be
		 *            <code>null</code>.
		 * @param object
		 *            Object to retrieve value from.
		 * @param remainingName
		 *            Remaining name of the property.
		 * @return Value as per processing.
		 * @throws Exception
		 *             If fails to process.
		 */
		R process(RetrieveStruct retrieveStruct, Object object,
				String remainingName) throws Exception;
	}

	/**
	 * Retrieve struct.
	 */
	private static class RetrieveStruct {

		/**
		 * {@link ValueRetriever}.
		 */
		public final ValueRetriever<Object> retriever;

		/**
		 * {@link PropertyMetaData}.
		 */
		public final PropertyMetaData metaData;

		/**
		 * Initiate.
		 * 
		 * @param retriever
		 *            {@link ValueRetriever}.
		 * @param metaData
		 *            {@link PropertyMetaData}.
		 */
		public RetrieveStruct(ValueRetriever<Object> retriever,
				PropertyMetaData metaData) {
			this.retriever = retriever;
			this.metaData = metaData;
		}
	}

}