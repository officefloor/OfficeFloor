/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.section;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.text.StringEscapeUtils;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.NotEscaped;
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
	 *             If {@link Method} to obtain the value to write is not available
	 *             on the bean type.
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
		NotEscaped annotation = this.valueRetriever.getValueAnnotation(this.propertyName, NotEscaped.class);
		this.isEscaped = (annotation == null);
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
