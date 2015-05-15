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
package net.officefloor.plugin.web.http.template;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.value.retriever.ValueRetriever;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.PropertyHttpTemplateSectionContent;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * {@link HttpTemplateWriter} to write a bean property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * {@link ValueRetriever}.
	 */
	private final ValueRetriever<Object> valueRetriever;

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * Indicates if the property is to be rendered escaped.
	 */
	private final boolean isEscaped;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link PropertyHttpTemplateSectionContent}.
	 * @param valueRetriever
	 *            {@link ValueRetriever}.
	 * @param beanType
	 *            Bean type for the property.
	 * @throws Exception
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public PropertyHttpTemplateWriter(
			PropertyHttpTemplateSectionContent content,
			ValueRetriever<Object> valueRetriever, Class<?> beanType)
			throws Exception {
		this.valueRetriever = valueRetriever;
		this.propertyName = content.getPropertyName();

		// Ensure the property is retrievable
		Method method = this.valueRetriever.getTypeMethod(this.propertyName);
		if (method == null) {
			throw new Exception("Property '" + this.propertyName
					+ "' can not be sourced from bean type "
					+ beanType.getName());
		}

		// Determine if should be escaped
		this.isEscaped = !(method.isAnnotationPresent(UnescapedHtml.class));
	}

	/*
	 * ================= HttpTemplateWriter ===============
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset,
			Object bean, HttpApplicationLocation location) throws IOException {

		// If no bean, then no value to output
		if (bean == null) {
			return;
		}

		// Obtain the property text value
		String propertyTextValue;
		try {

			// Obtain the property value from bean
			Object value = this.valueRetriever.retrieveValue(bean,
					this.propertyName);

			// Obtain the text value to write as content
			propertyTextValue = (value == null ? "" : value.toString());

		} catch (InvocationTargetException ex) {
			// Propagate cause of method failure
			throw new IOException(ex.getCause());
		} catch (Exception ex) {
			// Propagate failure
			throw new IOException(ex);
		}

		// Escape the value for HTML
		if (this.isEscaped) {
			propertyTextValue = StringEscapeUtils.escapeHtml(propertyTextValue);
		}

		// Write the text
		writer.write(propertyTextValue);
	}

}