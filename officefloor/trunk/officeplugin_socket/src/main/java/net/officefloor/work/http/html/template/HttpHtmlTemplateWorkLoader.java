/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.work.http.html.template;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;
import net.officefloor.work.http.HttpException;
import net.officefloor.work.http.HttpResponseSendTask;
import net.officefloor.work.http.html.template.parse.ReferenceTemplateSectionContent;
import net.officefloor.work.http.html.template.parse.StaticTemplateSectionContent;
import net.officefloor.work.http.html.template.parse.Template;
import net.officefloor.work.http.html.template.parse.TemplateSection;
import net.officefloor.work.http.html.template.parse.TemplateSectionContent;

/**
 * {@link WorkLoader} that loads a HTTP HTML template.
 * 
 * @author Daniel
 */
public class HttpHtmlTemplateWorkLoader extends AbstractWorkLoader {

	/**
	 * Property to specify the {@link Template} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.file.path";

	/**
	 * Property prefix to obtain the bean for the {@link TemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = "bean.";

	/*
	 * =================== AbstractWorkLoader =========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor
	 * .work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_FILE, "template");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.work.WorkLoader#loadWork(net.officefloor.work.
	 * WorkLoaderContext)
	 */
	@Override
	public WorkModel<?> loadWork(WorkLoaderContext context) throws Exception {

		// Obtain the template
		String templateFilePath = context.getProperty(PROPERTY_TEMPLATE_FILE);

		// Load the template
		InputStream configuration = context.getClassLoader()
				.getResourceAsStream(templateFilePath);
		if (configuration == null) {
			throw new Exception("Can not find template '" + templateFilePath
					+ "'");
		}
		Template template = Template.parse(configuration);

		// Create the tasks for each section of the template
		List<TaskModel<Indexed, None>> tasks = new LinkedList<TaskModel<Indexed, None>>();
		for (TemplateSection section : template.getSections()) {

			// Create the task model
			String sectionAndTaskName = section.getName();
			TaskModel<Indexed, None> task = new TaskModel<Indexed, None>();
			task.setTaskName(sectionAndTaskName);

			// Reference the HTTP connection to write template
			TaskObjectModel<Indexed> httpConnection = new TaskObjectModel<Indexed>();
			httpConnection.setObjectType(ServerHttpConnection.class.getName());
			task.addObject(httpConnection);

			// Indicate the IO escalation from the task
			TaskEscalationModel ioEscalation = new TaskEscalationModel();
			ioEscalation.setEscalationType(IOException.class.getName());
			task.addEscalation(ioEscalation);

			// Indicate the HTTP escalation from the task
			TaskEscalationModel httpEscalation = new TaskEscalationModel();
			httpEscalation.setEscalationType(HttpException.class.getName());
			task.addEscalation(httpEscalation);

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

						// Add the task object to link in the bean
						TaskObjectModel<Indexed> parameter = new TaskObjectModel<Indexed>();
						parameter.setObjectType(beanClass.getName());
						task.addObject(parameter);
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

			// Create and load the task factory manufacturer for the section
			boolean isRequireBean = (beanClass != null);
			HttpHtmlTemplateTask taskFactoryManufacturer = new HttpHtmlTemplateTask(
					isRequireBean, contentWriters
							.toArray(new HttpHtmlTemplateContentWriter[0]));
			task.setTaskFactoryManufacturer(taskFactoryManufacturer);

			// Add the task
			tasks.add(task);
		}

		// Add the send HTTP response task
		tasks.add(HttpResponseSendTask.createTaskModel());

		// Create the work
		WorkModel<HttpHtmlTemplateWork> work = new WorkModel<HttpHtmlTemplateWork>(
				HttpHtmlTemplateWork.class, new HttpHtmlTemplateWork(), tasks
						.toArray(new TaskModel[0]));

		// Return the work
		return work;
	}

}
