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
package net.officefloor.plugin.jdbc.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for reflection operations.
 * 
 * @author Daniel Sagenschneider
 */
public class ReflectionUtil {

	/**
	 * Creates a Bean with properties populated.
	 * 
	 * @param beanClassName
	 *            Name of the Bean {@link Class}.
	 * @param classLoader
	 *            {@link ClassLoader} to use to obtain the Bean {@link Class}.
	 * @param beanExpectedType
	 *            Expected type of the Bean.
	 * @param properties
	 *            Properties to configure the Bean.
	 * @return Bean.
	 * @throws Exception
	 *             If fails to create and populate the Bean.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T createInitialisedBean(String beanClassName,
			ClassLoader classLoader, Class<T> beanExpectedType,
			Properties properties) throws Exception {

		// Obtain the bean class
		Class beanClass = classLoader.loadClass(beanClassName);

		// Ensure instance is of expected type
		if (!beanExpectedType.isAssignableFrom(beanClass)) {
			throw new Exception("Bean class is not of expected type [class: "
					+ beanClassName + ", type: " + beanExpectedType.getName()
					+ "]");
		}

		// Obtain instance of bean
		Object bean = createInitialisedBean(beanClass, properties);

		// Return typed (already validated on class to be correct type)
		return (T) bean;
	}

	/**
	 * Creates the Bean with properties populated.
	 * 
	 * @param BeanClass
	 *            {@link Class} of the Bean.
	 * @param properties
	 *            Properties to configure the Bean.
	 * @return Bean.
	 * @throws Exception
	 *             If fails to create and populate the Bean.
	 */
	public static <T> T createInitialisedBean(Class<T> beanClass,
			Properties properties) throws Exception {

		// Create an instance of the Bean
		T bean = beanClass.newInstance();

		// Load the properties for the Bean
		for (Setter<T> setter : ReflectionUtil.getSetters(beanClass)) {

			// Obtain the property value
			String propertyName = setter.getPropertyName();
			String propertyValue = properties.getProperty(propertyName);
			if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
				// Property not configured, so do not load
				continue;
			}

			// Load the property value
			setter.setPropertyValue(bean, propertyValue);
		}

		// Return the configured Bean
		return bean;
	}

	/**
	 * Obtains the {@link Setter} instances from the input bean {@link Class}.
	 * 
	 * @param clazz
	 *            Bean {@link Class}.
	 * @return {@link Setter} instances.
	 */
	@SuppressWarnings("unchecked")
	public static <B> Setter<B>[] getSetters(Class<B> clazz) {

		// Obtain the setters from the class
		List<Setter<B>> setters = new LinkedList<Setter<B>>();
		for (Method method : clazz.getMethods()) {

			// Ensure the method is a public setter with only one argument
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!method.getName().startsWith("set")) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}

			// Create and add the setter
			Setter<B> setter = new Setter<B>(clazz, method);
			setters.add(setter);
		}

		// Return the setters
		return setters.toArray(new Setter[0]);
	}

	/**
	 * All access via static methods.
	 */
	private ReflectionUtil() {
	}
}
