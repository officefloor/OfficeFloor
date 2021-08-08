/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.value.retrieve;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.server.http.HttpException;

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
	public RootValueRetrieverImpl(PropertyMetaData[] properties, boolean isCaseInsensitive) {
		this(properties, isCaseInsensitive, new HashMap<PropertyMetaData, ValueRetriever<?>>());
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
	RootValueRetrieverImpl(PropertyMetaData[] properties, boolean isCaseInsensitive,
			Map<PropertyMetaData, ValueRetriever<?>> valueRetrieverByMetaData) {
		this.isCaseInsensitive = isCaseInsensitive;

		// Load the property retriever
		for (PropertyMetaData property : properties) {

			// Obtain the property name
			String propertyName = property.getPropertyName();

			// Determine if already registered value retriever
			ValueRetriever propertyRetriever = valueRetrieverByMetaData.get(property);
			if (propertyRetriever == null) {
				// Create the property retriever (registers itself)
				propertyRetriever = new PropertyValueRetrieverImpl<Object>(property, this.isCaseInsensitive,
						valueRetrieverByMetaData);
			}

			// Register the property retriever
			this.propertyToRetriever.put(propertyName, new RetrieveStruct(propertyRetriever, property));
		}
	}

	/*
	 * ===================== ValueRetreiver ======================
	 */

	/**
	 * {@link Processor} to obtain value type.
	 */
	private static Processor<Class<?>> valueTypeRetriever = (propertyRetriever, object, remainingName) -> {

		// Must be retriever to obtain value
		if (propertyRetriever == null) {
			return null; // property not retrievable
		}

		// Determine if further property
		if (remainingName.length() == 0) {
			// No further properties, so provide value type
			return propertyRetriever.metaData.getValueType();
		}

		// Delegate to obtain type method
		return propertyRetriever.retriever.getValueType(remainingName);
	};

	@Override
	public Class<?> getValueType(String name) throws HttpException {
		// Return type method
		return this.process(name, null, valueTypeRetriever);
	}

	@Override
	public <A> A getValueAnnotation(String name, Class<A> annotationType) throws HttpException {
		return this.process(name, null, (propertyRetriever, object, remainingName) -> {

			// Must be retriever to obtain value
			if (propertyRetriever == null) {
				return null; // no property, no annotation
			}

			// Determine if further property
			if (remainingName.length() == 0) {
				// No further properties, so provide value annotation
				return propertyRetriever.metaData.getValueAnnotation(annotationType);
			}

			// Delegate to obtain value annoation
			return propertyRetriever.retriever.getValueAnnotation(remainingName, annotationType);
		});
	}

	/**
	 * {@link Processor} to retrieve the value.
	 */
	private static Processor<Object> valueRetriever = (propertyRetriever, object, remainingName) -> {

		// Ensure able to retrieve value
		if (propertyRetriever == null) {
			return null; // Unknown value
		}

		// Return the retrieved value
		return propertyRetriever.retriever.retrieveValue(object, remainingName);
	};

	@Override
	public Object retrieveValue(T object, String name) throws HttpException {
		// Return the retrieved value
		return this.process(name, object, valueRetriever);
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
	 * @throws HttpException
	 *             If fails to process.
	 */
	private <R> R process(String name, Object object, Processor<R> processor) throws HttpException {

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
		RetrieveStruct retrieveStruct = this.propertyToRetriever.get(propertyName);

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
		 * @throws HttpException
		 *             If fails to process.
		 */
		R process(RetrieveStruct retrieveStruct, Object object, String remainingName) throws HttpException;
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
		public RetrieveStruct(ValueRetriever<Object> retriever, PropertyMetaData metaData) {
			this.retriever = retriever;
			this.metaData = metaData;
		}
	}

}
