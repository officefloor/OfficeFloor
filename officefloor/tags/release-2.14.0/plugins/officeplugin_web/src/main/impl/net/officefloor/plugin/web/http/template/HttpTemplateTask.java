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
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.value.retriever.ValueRetriever;
import net.officefloor.plugin.value.retriever.ValueRetrieverSource;
import net.officefloor.plugin.value.retriever.ValueRetrieverSourceImpl;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.BeanHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.PropertyHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContent;

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
	 * Property prefix to obtain whether the link is to be secure.
	 */
	public static final String PROPERTY_LINK_SECURE_PREFIX = "link.secure.";

	/**
	 * Loads the {@link TaskType} to write the {@link HttpTemplateSection}.
	 * 
	 * @param section
	 *            {@link HttpTemplateSection}.
	 * @param serverDefaultCharset
	 *            Default {@link Charset} for Server.
	 * @param templateUriPath
	 *            URI path for the {@link HttpTemplate}.
	 * @param templateUriSuffix
	 *            URI suffix for the {@link HttpTemplate} link URI paths. May be
	 *            <code>null</code> for no suffix.
	 * @param isTemplateSecure
	 *            Indicates if the template is to be secure.
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @param context
	 *            {@link WorkSourceContext}.
	 * @return Listing of {@link Task} names to handle {@link HttpTemplate} link
	 *         requests.
	 * @throws Exception
	 *             If fails to prepare the template.
	 */
	public static String[] loadTaskType(HttpTemplateSection section,
			Charset serverDefaultCharset, String templateUriPath,
			String templateUriSuffix, boolean isTemplateSecure,
			WorkTypeBuilder<HttpTemplateWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the section and task name
		String sectionAndTaskName = section.getSectionName();

		// Set of link task names
		Set<String> linkTaskNames = new HashSet<String>();

		// Optional bean class
		Class<?> beanClass = getBeanClass(sectionAndTaskName, false, context);

		// Create the content writers for the section
		SectionWriterStruct writerStruct = createHttpTemplateWriters(
				section.getContent(), beanClass, sectionAndTaskName,
				linkTaskNames, serverDefaultCharset, templateUriPath,
				templateUriSuffix, isTemplateSecure, context);

		// Determine if bean
		boolean isBean = (writerStruct.beanClass != null);

		// Create the task factory
		HttpTemplateTask task = new HttpTemplateTask(writerStruct.writers,
				isBean);

		// Define the task to write the section
		TaskTypeBuilder<Indexed, None> taskBuilder = workTypeBuilder
				.addTaskType(sectionAndTaskName, task, Indexed.class,
						None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		taskBuilder.addObject(HttpApplicationLocation.class).setLabel(
				"HTTP_APPLICATION_LOCATION");
		if (isBean) {
			taskBuilder.addObject(writerStruct.beanClass).setLabel("OBJECT");
		}
		taskBuilder.addEscalation(IOException.class);

		// Return the link task names
		return linkTaskNames.toArray(new String[0]);
	}

	/**
	 * Determines if the link should be secure.
	 * 
	 * @param linkName
	 *            Name of link.
	 * @param isTemplateSecure
	 *            Indicates whether the {@link HttpTemplate} is secure.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return <code>true</code> should the link be secure.
	 */
	public static boolean isLinkSecure(String linkName,
			boolean isTemplateSecure, SourceProperties properties) {

		// Determine if the link should be secure
		boolean isLinkSecure = Boolean.parseBoolean(properties.getProperty(
				PROPERTY_LINK_SECURE_PREFIX + linkName,
				String.valueOf(isTemplateSecure)));

		// Return whether secure
		return isLinkSecure;
	}

	/**
	 * Section {@link HttpTemplateWriter} struct.
	 */
	private static class SectionWriterStruct {

		/**
		 * {@link HttpTemplateWriter} instances.
		 */
		public final HttpTemplateWriter[] writers;

		/**
		 * Bean class. <code>null</code> indicates no bean required.
		 */
		public final Class<?> beanClass;

		/**
		 * Initiate.
		 * 
		 * @param writers
		 *            {@link HttpTemplateWriter} instances.
		 * @param beanClass
		 *            Bean class.
		 */
		public SectionWriterStruct(HttpTemplateWriter[] writers,
				Class<?> beanClass) {
			this.writers = writers;
			this.beanClass = beanClass;
		}
	}

	/**
	 * Obtains the {@link SectionWriterStruct}.
	 * 
	 * @param contents
	 *            {@link HttpTemplateSectionContent} instances.
	 * @param beanClass
	 *            Bean {@link Class}.
	 * @param sectionAndTaskName
	 *            Section and task name.
	 * @param linkTaskNames
	 *            List task names.
	 * @param serverDefaultCharset
	 *            Default {@link Charset} for the Server.
	 * @param templateUriPath
	 *            URI path for the {@link HttpTemplate}.
	 * @param templateUriSuffix
	 *            URI suffix for the {@link HttpTemplate} link URI paths. May be
	 *            <code>null</code> for no suffix.
	 * @param isTemplateSecure
	 *            Indicates if the template is to be secure.
	 * @param context
	 *            {@link WorkSourceContext}.
	 * @return {@link SectionWriterStruct}.
	 * @throws Exception
	 *             If fails to create the {@link SectionWriterStruct}.
	 */
	private static SectionWriterStruct createHttpTemplateWriters(
			HttpTemplateSectionContent[] contents, Class<?> beanClass,
			String sectionAndTaskName, Set<String> linkTaskNames,
			Charset serverDefaultCharset, String templateUriPath,
			String templateUriSuffix, boolean isTemplateSecure,
			WorkSourceContext context) throws Exception {

		// Create the content writers for the section
		List<HttpTemplateWriter> contentWriterList = new LinkedList<HttpTemplateWriter>();
		ValueRetriever<Object> valueRetriever = null;
		for (HttpTemplateSectionContent content : contents) {

			// Handle based on type
			if (content instanceof StaticHttpTemplateSectionContent) {
				// Add the static template writer
				StaticHttpTemplateSectionContent staticContent = (StaticHttpTemplateSectionContent) content;
				contentWriterList.add(new StaticHttpTemplateWriter(
						staticContent, serverDefaultCharset));

			} else if (content instanceof BeanHttpTemplateSectionContent) {
				// Add the bean template writer
				BeanHttpTemplateSectionContent beanContent = (BeanHttpTemplateSectionContent) content;

				// Ensure have bean class
				if (beanClass == null) {
					beanClass = getBeanClass(sectionAndTaskName, true, context);
				}

				// Ensure have the value loader for the bean
				if (valueRetriever == null) {
					valueRetriever = createValueRetriever(beanClass);
				}

				// Obtain the bean method
				String beanPropertyName = beanContent.getPropertyName();
				Method beanMethod = valueRetriever
						.getTypeMethod(beanPropertyName);
				if (beanMethod == null) {
					throw new Exception("Bean '" + beanPropertyName
							+ "' can not be sourced from bean type "
							+ beanClass.getName());
				}

				// Determine if an array of beans
				boolean isArray = false;
				Class<?> beanType = beanMethod.getReturnType();
				if (beanType.isArray()) {
					isArray = true;
					beanType = beanType.getComponentType();
				}

				// Obtain the writers for the bean
				SectionWriterStruct beanStruct = createHttpTemplateWriters(
						beanContent.getContent(), beanType, null,
						linkTaskNames, serverDefaultCharset, templateUriPath,
						templateUriSuffix, isTemplateSecure, context);

				// Add the content writer
				contentWriterList.add(new BeanHttpTemplateWriter(beanContent,
						valueRetriever, isArray, beanStruct.writers));

			} else if (content instanceof PropertyHttpTemplateSectionContent) {
				// Add the property template writer
				PropertyHttpTemplateSectionContent propertyContent = (PropertyHttpTemplateSectionContent) content;

				// Ensure have bean class
				if (beanClass == null) {
					beanClass = getBeanClass(sectionAndTaskName, true, context);
				}

				// Ensure have the value loader for the bean
				if (valueRetriever == null) {
					valueRetriever = createValueRetriever(beanClass);
				}

				// Add the content writer
				contentWriterList.add(new PropertyHttpTemplateWriter(
						propertyContent, valueRetriever, beanClass));

			} else if (content instanceof LinkHttpTemplateSectionContent) {
				// Add the link template writer
				LinkHttpTemplateSectionContent linkContent = (LinkHttpTemplateSectionContent) content;

				// Determine if the link is to be secure
				String linkName = linkContent.getName();
				boolean isLinkSecure = isLinkSecure(linkName, isTemplateSecure,
						context);

				// Add the content writer
				contentWriterList.add(new LinkHttpTemplateWriter(linkContent,
						templateUriPath, templateUriSuffix, isLinkSecure));

				// Track the link tasks
				linkTaskNames.add(linkName);

			} else {
				// Unknown content
				throw new IllegalStateException("Unknown content type '"
						+ content.getClass().getName());
			}
		}

		// Return the HTTP Template writers
		return new SectionWriterStruct(
				contentWriterList.toArray(new HttpTemplateWriter[contentWriterList
						.size()]), beanClass);
	}

	/**
	 * Obtains the bean {@link Class}.
	 * 
	 * @param sectionAndTaskName
	 *            Section and task name.
	 * @param context
	 *            {@link WorkSourceContext}.
	 * @return Bean {@link Class}.
	 */
	private static Class<?> getBeanClass(String sectionAndTaskName,
			boolean isRequired, WorkSourceContext context) {

		// Obtain the bean class name
		String beanClassPropertyName = PROPERTY_BEAN_PREFIX
				+ sectionAndTaskName;
		String beanClassName;
		if (isRequired) {
			// Must provide bean class name
			beanClassName = context.getProperty(beanClassPropertyName);

		} else {
			// Optionally provide bean class name
			beanClassName = context.getProperty(beanClassPropertyName, null);
			if (beanClassName == null) {
				return null; // No class name, no bean
			}
		}

		// Obtain the class
		Class<?> beanClass = context.loadClass(beanClassName);

		// Return the class
		return beanClass;
	}

	/**
	 * Creates the {@link ValueRetriever}.
	 * 
	 * @param beanClass
	 *            {@link Class} of the bean. May be <code>null</code> if top
	 *            level content.
	 * @return {@link ValueRetriever}.
	 * @throws Exception
	 *             If fails to create the {@link ValueRetriever}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ValueRetriever<Object> createValueRetriever(
			Class<?> beanClass) throws Exception {

		// Obtain the value retriever
		ValueRetrieverSource source = new ValueRetrieverSourceImpl();
		source.init(true);
		ValueRetriever valueRetriever = source.sourceValueRetriever(beanClass);

		// Return the value retriever
		return valueRetriever;
	}

	/**
	 * {@link HttpTemplateWriter} instances to write the content.
	 */
	private final HttpTemplateWriter[] contentWriters;

	/**
	 * Flag indicating if a bean.
	 */
	private final boolean isBean;

	/**
	 * Initiate.
	 * 
	 * @param contentWriters
	 *            {@link HttpTemplateWriter} instances to write the content.
	 * @param isRequireBean
	 *            Flag indicating if a bean.
	 */
	public HttpTemplateTask(HttpTemplateWriter[] contentWriters, boolean isBean) {
		this.contentWriters = contentWriters;
		this.isBean = isBean;
	}

	/*
	 * ======================= Task ================================
	 */

	@Override
	public Object doTask(TaskContext<HttpTemplateWork, Indexed, None> context)
			throws IOException {

		// Obtain the bean dependency
		Object bean;
		if (this.isBean) {
			// Obtain the bean
			bean = context.getObject(2);

			// No bean, no content
			if (bean == null) {
				return null;
			}

		} else {
			// No bean
			bean = null;
		}

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpApplicationLocation location = (HttpApplicationLocation) context
				.getObject(1);

		// Obtain the writer
		HttpResponse response = connection.getHttpResponse();
		ServerWriter writer = response.getEntityWriter();

		// Write the contents
		for (HttpTemplateWriter contentWriter : this.contentWriters) {
			contentWriter.write(writer, bean, location);
		}

		// Flush contents
		writer.flush();

		// Template written, nothing to return
		return null;
	}

}