/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.template;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.value.retriever.ValueRetriever;
import net.officefloor.plugin.web.http.template.HttpTemplateWriter;
import net.officefloor.plugin.web.http.template.parse.PropertyHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write a bean property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * <code>Content-Type</code>.
	 */
	private final String contentType;

	/**
	 * {@link ValueRetriever}.
	 */
	private final ValueRetriever<Object> valueRetriever;

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link PropertyHttpTemplateSectionContent}.
	 * @param valueRetriever
	 *            {@link ValueRetriever}.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @param beanType
	 *            Bean type for the property.
	 * @throws Exception
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public PropertyHttpTemplateWriter(
			PropertyHttpTemplateSectionContent content,
			ValueRetriever<Object> valueRetriever, String contentType,
			Class<?> beanType) throws Exception {
		this.contentType = contentType;
		this.valueRetriever = valueRetriever;
		this.propertyName = content.getPropertyName();

		// Ensure the property is retrievable
		if (!this.valueRetriever.isValueRetrievable(this.propertyName)) {
			throw new Exception("Property '" + this.propertyName
					+ "' can not be sourced from bean type "
					+ beanType.getName());
		}
	}

	/*
	 * ================= HttpTemplateWriter ===============
	 */

	@Override
	public void write(HttpResponseWriter writer, String workName, Object bean)
			throws IOException {

		// If no bean, then no value to output
		if (bean == null) {
			return;
		}

		// Obtain the property text value
		String propertyTextValue;
		try {

			// Obtain the property value from bean
			String value = this.valueRetriever.retrieveValue(bean,
					this.propertyName);

			// Obtain the text value to write as content
			propertyTextValue = (value == null ? "" : value);

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