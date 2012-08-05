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

import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.value.retriever.ValueRetriever;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.BeanHttpTemplateSectionContent;

/**
 * {@link BeanTemplateWriter} to write a bean.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanHttpTemplateWriter implements HttpTemplateWriter {

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
	 * {@link HttpTemplateWriter} instances for the bean.
	 */
	private final HttpTemplateWriter[] beanWriters;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link BeanHttpTemplateSectionContent}.
	 * @param valueRetriever
	 *            {@link ValueRetriever}.
	 * @param isArray
	 *            Indicates if an array of beans to render.
	 * @param beanWriters
	 *            {@link HttpTemplateWriter} instances for the bean.
	 * @throws Exception
	 *             If {@link Method} to obtain the value to write is not
	 *             available on the bean type.
	 */
	public BeanHttpTemplateWriter(BeanHttpTemplateSectionContent content,
			ValueRetriever<Object> valueRetriever, boolean isArray,
			HttpTemplateWriter[] beanWriters) throws Exception {
		this.valueRetriever = valueRetriever;
		this.propertyName = content.getPropertyName();
		this.isArray = isArray;
		this.beanWriters = beanWriters;
	}

	/*
	 * ================= HttpTemplateWriter ===============
	 */

	@Override
	public void write(ServerWriter writer, String workName, Object bean,
			HttpApplicationLocation location) throws IOException {

		// If no bean, then no value to output
		if (bean == null) {
			return;
		}

		// Obtain the bean
		Object writerBean;
		try {

			// Obtain the bean
			writerBean = this.valueRetriever.retrieveValue(bean,
					this.propertyName);

		} catch (InvocationTargetException ex) {
			// Propagate cause of method failure
			throw new IOException(ex.getCause());
		} catch (Exception ex) {
			// Propagate failure
			throw new IOException(ex);
		}

		// Only write content if have the bean
		if (writerBean == null) {
			return;
		}

		// Determine if an array
		if (this.isArray) {
			// Write the content for the array of beans
			Object[] arrayBeans = (Object[]) writerBean;
			for (Object arrayBean : arrayBeans) {
				for (HttpTemplateWriter beanWriter : this.beanWriters) {
					beanWriter.write(writer, workName, arrayBean, location);
				}
			}

		} else {
			// Write the content for the bean
			for (HttpTemplateWriter beanWriter : this.beanWriters) {
				beanWriter.write(writer, workName, writerBean, location);
			}
		}
	}

}