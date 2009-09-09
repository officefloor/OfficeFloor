/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.work.http.html.template;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.work.http.HttpException;

/**
 * {@link HttpHtmlTemplateContentWriter} to write a bean's property.
 *
 * @author Daniel Sagenschneider
 */
public class BeanPropertyHttpHtmlTemplateContentWriter implements
		HttpHtmlTemplateContentWriter {

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
	 * Initiate.
	 *
	 * @param beanSuperType
	 *            Bean type. The bean's class will either be an instance of this
	 *            type or a sub type of it.
	 * @param propertyMethod
	 *            {@link Method} to obtain the property value from the bean.
	 */
	public BeanPropertyHttpHtmlTemplateContentWriter(Class<?> beanSuperType,
			Method propertyMethod) {
		this.beanTypeToPropertyMap.put(beanSuperType, propertyMethod);
		this.propertyMethodName = propertyMethod.getName();
	}

	@Override
	public void writeContent(Object bean, Writer httpBody,
			HttpResponse httpResponse) throws IOException, HttpException {

		// Obtain the property value
		String propertyTextValue;
		try {

			// Obtain the method to obtain the value
			Class<?> beanClass = bean.getClass();
			Method propertyMethod;
			synchronized (this.beanTypeToPropertyMap) {
				propertyMethod = this.beanTypeToPropertyMap.get(beanClass);
				if (propertyMethod == null) {
					// New bean type, so obtain property method for it
					propertyMethod = beanClass
							.getMethod(this.propertyMethodName);

					// Cache property method, for next time
					this.beanTypeToPropertyMap.put(beanClass, propertyMethod);
				}
			}

			// Obtain the property value from bean
			Object value = propertyMethod.invoke(bean);

			// Obtain the text value to write as content
			propertyTextValue = (value == null ? "" : value.toString());

		} catch (Throwable ex) {
			// Propagate failure
			throw new HttpException(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex);
		}

		// Write the text
		httpBody.write(propertyTextValue);
		httpBody.flush();
	}

}
