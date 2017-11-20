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
package net.officefloor.web.template.section;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringEscapeUtils;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.parse.PropertyParsedTemplateSectionContent;
import net.officefloor.web.value.retrieve.ValueRetriever;

/**
 * {@link WebTemplateWriter} to write a bean property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyWebTemplateWriter implements WebTemplateWriter {

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
	 *            {@link PropertyParsedTemplateSectionContent}.
	 * @param valueRetriever
	 *            {@link ValueRetriever}.
	 * @param beanType
	 *            Bean type for the property.
	 * @throws Exception
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public PropertyWebTemplateWriter(PropertyParsedTemplateSectionContent content,
			ValueRetriever<Object> valueRetriever, Class<?> beanType) throws Exception {
		this.valueRetriever = valueRetriever;
		this.propertyName = content.getPropertyName();

		// Ensure the property is retrievable
		Class<?> valueType = this.valueRetriever.getValueType(this.propertyName);
		if (valueType == null) {
			throw new Exception(
					"Property '" + this.propertyName + "' can not be sourced from bean type " + beanType.getName());
		}

		// Determine if should be escaped
		// TODO determine means to obtain annotations from ValueRetriever
		this.isEscaped = true; // !(method.isAnnotationPresent(NotEscaped.class));
	}

	/*
	 * ================= WebTemplateWriter ===============
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException {

		// If no bean, then no value to output
		if (bean == null) {
			return;
		}

		// Obtain the property value from bean
		Object value = this.valueRetriever.retrieveValue(bean, this.propertyName);

		// Obtain the text value to write as content
		String propertyTextValue = (value == null ? "" : value.toString());

		try {
			// Write out the value
			if (this.isEscaped) {
				// Write the escaped value
				StringEscapeUtils.ESCAPE_HTML4.translate(propertyTextValue, writer);
			} else {
				// Write the raw value
				writer.write(propertyTextValue);
			}

		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}