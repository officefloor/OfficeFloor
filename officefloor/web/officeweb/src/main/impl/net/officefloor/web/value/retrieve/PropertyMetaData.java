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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meta-data for the property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyMetaData {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} for the type.
	 */
	private final Method typeMethod;

	/**
	 * Mapping of {@link Method} to the particular type.
	 */
	private final Map<Class<?>, Method> typeToMethod = new ConcurrentHashMap<Class<?>, Method>();

	/**
	 * Properties on the resulting property object.
	 */
	private final PropertyMetaData[] properties;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param typeMethod
	 *            {@link Method} for the type.
	 * @param properties
	 *            Properties on the resulting property object.
	 */
	public PropertyMetaData(String propertyName, Method typeMethod, PropertyMetaData[] properties) {
		this.propertyName = propertyName;
		this.typeMethod = typeMethod;
		this.properties = properties;
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return Property name.
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * <p>
	 * Obtains the type {@link Method}.
	 * <p>
	 * This is the {@link Method} base on the type and may be abstract.
	 * 
	 * @return Type {@link Method}.
	 */
	public Class<?> getValueType() {
		return this.typeMethod.getReturnType();
	}

	/**
	 * <p>
	 * Obtains the value {@link Annotation}.
	 * 
	 * @param <A>
	 *            {@link Annotation} type.
	 * @param annotationType
	 *            {@link Annotation}.
	 * @return {@link Annotation} or <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public <A> A getValueAnnotation(Class<A> annotationType) {

		// Ensure annotation type
		if (!(Annotation.class.isAssignableFrom(annotationType))) {
			return null; // must be annoation type
		}

		// Obtain the annotation
		Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) annotationType;
		return (A) this.typeMethod.getAnnotation(annotationClass);
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @param type
	 *            Type to obtain the {@link Method}.
	 * @return {@link Method}.
	 * @throws Exception
	 *             If fails to obtain the {@link Method}.
	 */
	public Method getMethod(Class<?> type) throws Exception {

		// Lazy obtain the method
		Method method = this.typeToMethod.get(type);
		if (method == null) {
			method = type.getMethod(this.typeMethod.getName());
			this.typeToMethod.put(type, method);
		}

		// Return the method
		return method;
	}

	/**
	 * Obtains the Properties on the resulting property object.
	 * 
	 * @return Properties on the resulting property object.
	 */
	public PropertyMetaData[] getProperties() {
		return this.properties;
	}

}
