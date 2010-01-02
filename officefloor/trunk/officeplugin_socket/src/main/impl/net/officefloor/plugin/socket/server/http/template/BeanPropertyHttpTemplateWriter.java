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

package net.officefloor.plugin.socket.server.http.template;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.socket.server.http.template.parse.ReferenceHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write a bean property.
 *
 * @author Daniel Sagenschneider
 */
public class BeanPropertyHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * {@link Method} to obtain the property value from the bean.
	 */
	private final Map<Class<?>, Method> beanTypeToPropertyMap = new HashMap<Class<?>, Method>(
			3);

	/**
	 * Name of the property {@link Method} on the bean.
	 */
	private final String propertyMethodName;

	/**
	 * <code>Content-Type</code>.
	 */
	private final String contentType;

	/**
	 * Initiate.
	 *
	 * @param content
	 *            {@link ReferenceHttpTemplateSectionContent}.
	 * @param beanType
	 *            Bean type. The bean class will either be an instance of this
	 *            type or a sub type of it.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @throws NoSuchMethodException
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public BeanPropertyHttpTemplateWriter(
			ReferenceHttpTemplateSectionContent content, Class<?> beanType,
			String contentType) throws NoSuchMethodException {
		this.contentType = contentType;

		// Obtain the property method name
		String propertyName = content.getKey();
		propertyName = (propertyName == null ? "" : propertyName.trim());
		if ((propertyName.length() == 0)
				|| ("toString".equalsIgnoreCase(propertyName))) {
			// Use 'toString' method of object
			this.propertyMethodName = "toString";
		} else {
			// Ensure first character is upper case
			propertyName = propertyName.trim();
			propertyName = propertyName.substring(0, 1).toUpperCase()
					+ propertyName.substring(1);

			// Use the 'getXxx' method
			this.propertyMethodName = "get" + propertyName;
		}

		// Obtain and register the property method for the bean type
		Method method = this.getPropertyMethod(beanType);

		// Ensure method is public
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new NoSuchMethodException("Method "
					+ beanType.getSimpleName() + "." + this.propertyMethodName
					+ "() must be public");
		}

		// Ensure method returns type (not void)
		Class<?> returnType = method.getReturnType();
		if ((returnType == null) || (returnType.equals(Void.TYPE))) {
			throw new NoSuchMethodException("Method "
					+ beanType.getSimpleName() + "." + this.propertyMethodName
					+ "() must return a value (not void)");
		}
	}

	/**
	 * Obtains the {@link Method} to obtain the value to write from the bean.
	 *
	 * @param beanClass
	 *            {@link Class} of the bean to find the {@link Method}.
	 * @return {@link Method} to obtain the value to write from the bean.
	 */
	private Method getPropertyMethod(Class<?> beanClass)
			throws NoSuchMethodException {
		synchronized (this.beanTypeToPropertyMap) {

			// Lazy load the property method
			Method propertyMethod = this.beanTypeToPropertyMap.get(beanClass);
			if (propertyMethod == null) {
				// New bean type, so obtain property method for it
				propertyMethod = beanClass.getMethod(this.propertyMethodName);

				// Cache property method, for next time
				this.beanTypeToPropertyMap.put(beanClass, propertyMethod);
			}

			// Return the property method
			return propertyMethod;
		}
	}

	/*
	 * ================= HttpTemplateWriter ===============
	 */

	@Override
	public void write(HttpResponseWriter writer, Object bean)
			throws IOException {

		// Obtain the property text value
		String propertyTextValue;
		try {

			// Obtain the method to obtain the value
			Class<?> beanClass = bean.getClass();
			Method propertyMethod = this.getPropertyMethod(beanClass);

			// Obtain the property value from bean
			Object value = propertyMethod.invoke(bean);

			// Obtain the text value to write as content
			propertyTextValue = (value == null ? "" : value.toString());

		} catch (InvocationTargetException ex) {
			// Propagate cause of method failure
			throw new IOException(ex.getCause());
		} catch (Exception ex) {
			// Propagate failure
			throw new IOException(ex);
		}

		// Write the text
		writer.write(this.contentType, propertyTextValue);
	}

}