/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.template.section;

import java.lang.reflect.Method;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.web.value.retrieve.ValueRetriever;

/**
 * {@link WebTemplateWriter} to write a bean.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanWebTemplateWriter implements WebTemplateWriter {

	/**
	 * {@link ValueRetriever}.
	 */
	private final ValueRetriever<Object> valueRetriever;

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * Flag indicating if an array of beans to render.
	 */
	private final boolean isArray;

	/**
	 * {@link WebTemplateWriter} instances for the bean.
	 */
	private final WebTemplateWriter[] beanWriters;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link BeanParsedTemplateSectionContent}.
	 * @param valueRetriever
	 *            {@link ValueRetriever}.
	 * @param isArray
	 *            Indicates if an array of beans to render.
	 * @param beanWriters
	 *            {@link WebTemplateWriter} instances for the bean.
	 * @throws Exception
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public BeanWebTemplateWriter(BeanParsedTemplateSectionContent content, ValueRetriever<Object> valueRetriever,
			boolean isArray, WebTemplateWriter[] beanWriters) throws Exception {
		this.valueRetriever = valueRetriever;
		this.propertyName = content.getPropertyName();
		this.isArray = isArray;
		this.beanWriters = beanWriters;
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

		// Obtain the bean
		Object writerBean = this.valueRetriever.retrieveValue(bean, this.propertyName);

		// Only write content if have the bean
		if (writerBean == null) {
			return;
		}

		// Determine if an array
		if (this.isArray) {
			// Write the content for the array of beans
			Object[] arrayBeans = (Object[]) writerBean;
			for (Object arrayBean : arrayBeans) {
				for (WebTemplateWriter beanWriter : this.beanWriters) {
					beanWriter.write(writer, isDefaultCharset, arrayBean, connection, templatePath);
				}
			}

		} else {
			// Write the content for the bean
			for (WebTemplateWriter beanWriter : this.beanWriters) {
				beanWriter.write(writer, isDefaultCharset, writerBean, connection, templatePath);
			}
		}
	}

}
