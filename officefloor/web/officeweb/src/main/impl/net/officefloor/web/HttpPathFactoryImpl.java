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

package net.officefloor.web;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpPathFactory;
import net.officefloor.web.value.retrieve.ValueRetriever;

/**
 * Factory to create the HTTP path.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPathFactoryImpl<T> implements HttpPathFactory<T> {

	/**
	 * Segment of the path.
	 */
	public static abstract class Segment<T> {

		/**
		 * Writes the values.
		 * 
		 * @param values
		 *            Values.
		 * @param target
		 *            {@link Appendable}.
		 * @throws Exception
		 *             Failure in writing value.
		 */
		protected abstract void write(T values, Appendable target) throws Exception;
	}

	/**
	 * Static {@link Segment}.
	 */
	public static class StaticSegment<T> extends Segment<T> {

		/**
		 * Static path.
		 */
		private final String staticPath;

		/**
		 * Instantiate.
		 * 
		 * @param staticPath
		 *            Static path.
		 */
		public StaticSegment(String staticPath) {
			this.staticPath = staticPath;
		}

		/*
		 * =================== Segment ===================
		 */

		@Override
		protected void write(Object values, Appendable target) throws Exception {
			target.append(this.staticPath);
		}
	}

	/**
	 * Parameter {@link Segment}.
	 */
	public static class ParameterSegment<T> extends Segment<T> {

		/**
		 * Property name to obtain value.
		 */
		private final String propertyName;

		/**
		 * {@link ValueRetriever} to obtain the value.
		 */
		private final ValueRetriever<T> valueRetriever;

		/**
		 * Instantiate.
		 * 
		 * @param propertyName
		 *            Property name to obtain value.
		 * @param valueRetriever
		 *            {@link ValueRetriever} to obtain the value.
		 */
		public ParameterSegment(String propertyName, ValueRetriever<T> valueRetriever) {
			this.propertyName = propertyName;
			this.valueRetriever = valueRetriever;
		}

		/*
		 * =================== Segment ===================
		 */

		@Override
		protected void write(T values, Appendable target) throws Exception {

			// Obtain the parameter value
			Object value = this.valueRetriever.retrieveValue(values, this.propertyName);
			if (value == null) {
				value = "";
			}

			// Write the value
			target.append(value.toString());
		}
	}

	/**
	 * {@link ThreadLocal} {@link StringBuilder}.
	 */
	private static ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}
	};

	/**
	 * Type to obtain values.
	 */
	private final Class<T> valuesType;

	/**
	 * {@link Segment} instances to create the path.
	 */
	private final Segment<T>[] segments;

	/**
	 * Instantiate.
	 * 
	 * @param valuesType
	 *            Type to obtain values.
	 * @param segments
	 *            {@link Segment} instances.
	 */
	public HttpPathFactoryImpl(Class<T> valuesType, Segment<T>[] segments) {
		this.valuesType = valuesType;
		this.segments = segments;
	}

	/*
	 * ==================== HttpPathFactory ====================
	 */

	@Override
	public Class<T> getValuesType() {
		return this.valuesType;
	}

	@Override
	public String createApplicationClientPath(T values) throws HttpException {
		try {

			// Obtain the string builder to create the path
			StringBuilder buffer = stringBuilder.get();
			buffer.setLength(0);

			// Generate the path
			for (int i = 0; i < this.segments.length; i++) {
				Segment<T> segment = this.segments[i];
				segment.write(values, buffer);
			}

			// Return the path
			return buffer.toString();

		} catch (Exception ex) {
			throw new HttpException(ex);
		}
	}

}
