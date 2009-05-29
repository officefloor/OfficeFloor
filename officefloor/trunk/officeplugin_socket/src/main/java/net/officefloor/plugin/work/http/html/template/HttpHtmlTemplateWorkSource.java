/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.HttpResponseSendTask;
import net.officefloor.plugin.work.http.HttpResponseSendTask.HttpResponseSendTaskDependencies;
import net.officefloor.plugin.work.http.html.template.HttpHtmlTemplateTask.HttpHtmlTemplateTaskDependencies;
import net.officefloor.plugin.work.http.html.template.parse.ReferenceTemplateSectionContent;
import net.officefloor.plugin.work.http.html.template.parse.StaticTemplateSectionContent;
import net.officefloor.plugin.work.http.html.template.parse.Template;
import net.officefloor.plugin.work.http.html.template.parse.TemplateSection;
import net.officefloor.plugin.work.http.html.template.parse.TemplateSectionContent;

/**
 * {@link WorkSource} that loads a HTTP HTML template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHtmlTemplateWorkSource extends
		AbstractWorkSource<HttpHtmlTemplateWork> {

	/**
	 * Property to specify the {@link Template} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.file.path";

	/**
	 * Property prefix to obtain the bean for the {@link TemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = "bean.";

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_FILE, "template");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpHtmlTemplateWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the template
		String templateFilePath = context.getProperty(PROPERTY_TEMPLATE_FILE);

		// Load the template
		InputStream configuration = context.getClassLoader()
				.getResourceAsStream(templateFilePath);
		if (configuration == null) {
			throw new FileNotFoundException("Can not find template '"
					+ templateFilePath + "'");
		}
		Template template = Template.parse(configuration);

		// Define the work factory
		workTypeBuilder.setWorkFactory(new HttpHtmlTemplateWork());

		// Define the tasks
		for (TemplateSection section : template.getSections()) {

			// Obtain the section and task name
			String sectionAndTaskName = section.getName();

			// Create the content writers for the section
			List<HttpHtmlTemplateContentWriter> contentWriters = new LinkedList<HttpHtmlTemplateContentWriter>();
			Class<?> beanClass = null;
			for (TemplateSectionContent content : section.getContents()) {

				if (content instanceof StaticTemplateSectionContent) {
					// Add the static content writer
					StaticTemplateSectionContent staticContent = (StaticTemplateSectionContent) content;
					contentWriters.add(new StaticHttpHtmlTemplateContentWriter(
							staticContent.getStaticContent()));

				} else if (content instanceof ReferenceTemplateSectionContent) {
					// Add the reference content writer
					ReferenceTemplateSectionContent referenceContent = (ReferenceTemplateSectionContent) content;

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

					// Obtain the method to get the bean property referenced
					String methodName = referenceContent.getKey();
					methodName = "get"
							+ methodName.substring(0, 1).toUpperCase()
							+ methodName.substring(1);
					Method method = beanClass.getMethod(methodName);

					// Ensure the method is accessible
					if (!Modifier.isPublic(method.getModifiers())) {
						throw new Exception(method.getName() + " for bean "
								+ beanClass.getName() + " of section "
								+ sectionAndTaskName + " must be public");
					}

					// Ensure the method returns a value
					Class<?> returnType = method.getReturnType();
					if ((returnType == null) || (returnType == Void.class)) {
						throw new Exception(method.getName() + " for bean "
								+ beanClass.getName() + " of section "
								+ sectionAndTaskName + " must not return void");
					}

					// Add the content writer
					contentWriters
							.add(new BeanPropertyHttpHtmlTemplateContentWriter(
									beanClass, method));

				} else {
					// Unknown content
					throw new Exception("Unknown content type '"
							+ content.getClass().getName());
				}
			}

			// Create the task factory manufacturer for the section
			boolean isRequireBean = (beanClass != null);
			HttpHtmlTemplateTask taskFactoryManufacturer = new HttpHtmlTemplateTask(
					isRequireBean, contentWriters
							.toArray(new HttpHtmlTemplateContentWriter[0]));

			// Default the bean class to Object if not require bean.
			// This will allow any argument type to the task.
			if (beanClass == null) {
				beanClass = Object.class;
			}

			// Define the task for the section
			TaskTypeBuilder<HttpHtmlTemplateTaskDependencies, None> taskBuilder = workTypeBuilder
					.addTaskType(sectionAndTaskName, taskFactoryManufacturer,
							HttpHtmlTemplateTaskDependencies.class, None.class);
			taskBuilder.addObject(ServerHttpConnection.class).setKey(
					HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION);
			taskBuilder.addObject(beanClass).setKey(
					HttpHtmlTemplateTaskDependencies.BEAN);
			taskBuilder.addEscalation(HttpException.class);
			taskBuilder.addEscalation(IOException.class);
		}

		// Include in the send response task
		TaskTypeBuilder<HttpResponseSendTaskDependencies, None> sendTaskBuilder = workTypeBuilder
				.addTaskType("SendHttpResponse", new HttpResponseSendTask(),
						HttpResponseSendTaskDependencies.class, None.class);
		sendTaskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		sendTaskBuilder.addEscalation(IOException.class);
	}

}