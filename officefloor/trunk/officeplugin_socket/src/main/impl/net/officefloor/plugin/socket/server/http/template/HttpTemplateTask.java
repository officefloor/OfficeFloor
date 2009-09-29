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
package net.officefloor.plugin.socket.server.http.template;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.socket.server.http.template.parse.ReferenceHttpTemplateSectionContent;
import net.officefloor.plugin.socket.server.http.template.parse.StaticHttpTemplateSectionContent;

/**
 * {@link Task} to write the {@link HttpTemplateSection}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpTemplateTask extends
		AbstractSingleTask<HttpTemplateWork, Indexed, None> {

	/**
	 * Property prefix to obtain the bean for the {@link HttpTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = "bean.";

	/**
	 * Loads the {@link TaskType} for to write the {@link HttpTemplateSection}.
	 *
	 * @param section
	 *            {@link HttpTemplateSection}.
	 * @param contentType
	 *            <code>Content-Type</code> for the result of the template.
	 * @param charset
	 *            {@link Charset} for the result of the template.
	 * @param writerFactory
	 *            {@link HttpResponseWriterFactory}.
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @param context
	 *            {@link WorkSourceContext}.
	 * @throws IOException
	 *             If fails to prepare the template.
	 * @throws ClassNotFoundException
	 *             If template bean can not be found.
	 * @throws NoSuchMethodException
	 *             If property can not be found on bean type.
	 */
	public static void loadTaskType(HttpTemplateSection section,
			String contentType, Charset charset,
			HttpResponseWriterFactory writerFactory,
			WorkTypeBuilder<HttpTemplateWork> workTypeBuilder,
			WorkSourceContext context) throws IOException,
			ClassNotFoundException, NoSuchMethodException {

		// Obtain the section and task name
		String sectionAndTaskName = section.getSectionName();

		// Create the content writers for the section
		List<HttpTemplateWriter> contentWriterList = new LinkedList<HttpTemplateWriter>();
		Class<?> beanClass = null;
		for (HttpTemplateSectionContent content : section.getContent()) {

			// Handle based on type
			if (content instanceof StaticHttpTemplateSectionContent) {
				// Add the static template writer
				StaticHttpTemplateSectionContent staticContent = (StaticHttpTemplateSectionContent) content;
				contentWriterList.add(new StaticHttpTemplateWriter(
						staticContent, contentType, charset));

			} else if (content instanceof ReferenceHttpTemplateSectionContent) {
				// Add the reference template writer
				ReferenceHttpTemplateSectionContent referenceContent = (ReferenceHttpTemplateSectionContent) content;

				// Ensure have the bean class
				if (beanClass == null) {
					String beanClassPropertyName = PROPERTY_BEAN_PREFIX
							+ sectionAndTaskName;
					String beanClassName = context
							.getProperty(beanClassPropertyName);

					// Obtain the class
					beanClass = context.getClassLoader().loadClass(
							beanClassName);
				}

				// Add the content writer
				contentWriterList.add(new BeanPropertyHttpTemplateWriter(
						referenceContent, beanClass, contentType));

			} else {
				// Unknown content
				throw new IllegalStateException("Unknown content type '"
						+ content.getClass().getName());
			}
		}

		// Determine if requires bean
		boolean isRequireBean = (beanClass != null);

		// Create the task factory
		HttpTemplateTask task = new HttpTemplateTask(contentWriterList
				.toArray(new HttpTemplateWriter[0]), isRequireBean,
				writerFactory);

		// Define the task to write the section
		TaskTypeBuilder<Indexed, None> taskBuilder = workTypeBuilder
				.addTaskType(sectionAndTaskName, task, Indexed.class,
						None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		if (isRequireBean) {
			taskBuilder.addObject(beanClass).setLabel("OBJECT");
		}
		taskBuilder.addEscalation(IOException.class);
	}

	/**
	 * {@link HttpTemplateWriter} instances to write the content.
	 */
	private final HttpTemplateWriter[] contentWriters;

	/**
	 * Flag indicating if a bean is required.
	 */
	private final boolean isRequireBean;

	/**
	 * {@link HttpResponseWriterFactory}.
	 */
	private final HttpResponseWriterFactory writerFactory;

	/**
	 * Initiate.
	 *
	 * @param contentWriters
	 *            {@link HttpTemplateWriter} instances to write the content.
	 * @param isRequireBean
	 *            Flag indicating if a bean is required.
	 * @param writerFactory
	 *            {@link HttpResponseWriterFactory}.
	 */
	public HttpTemplateTask(HttpTemplateWriter[] contentWriters,
			boolean isRequireBean, HttpResponseWriterFactory writerFactory) {
		this.contentWriters = contentWriters;
		this.isRequireBean = isRequireBean;
		this.writerFactory = writerFactory;
	}

	/*
	 * ======================= Task ================================
	 */

	@Override
	public Object doTask(TaskContext<HttpTemplateWork, Indexed, None> context)
			throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		Object bean = (this.isRequireBean ? context.getObject(1) : null);

		// Create the response writer
		HttpResponseWriter writer = this.writerFactory
				.createHttpResponseWriter(connection);

		// Write the contents
		for (HttpTemplateWriter contentWriter : this.contentWriters) {
			contentWriter.write(writer, bean);
		}

		// Template written, nothing to return
		return null;
	}

}