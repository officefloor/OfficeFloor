/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web;

import net.officefloor.web.value.retrieve.ValueRetriever;

/**
 * Factory to create the HTTP path.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPathFactoryImpl<T> {

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

	/**
	 * Obtains the type to obtain values.
	 * 
	 * @return Type to obtain values.
	 */
	public Class<T> getValuesType() {
		return this.valuesType;
	}

	/**
	 * Creates the path.
	 * 
	 * @param values
	 *            Object to retrieve the values from.
	 * @return Path.
	 * @throws Exception
	 *             If fails to create path.
	 */
	public <S extends T> String createPath(S values) throws Exception {

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
	}

}