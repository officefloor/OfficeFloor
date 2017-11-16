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
package net.officefloor.plugin.web.template;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.plugin.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.plugin.web.template.parse.LinkParsedTemplateSectionContent;
import net.officefloor.plugin.web.template.parse.ParsedTemplate;
import net.officefloor.plugin.web.template.parse.ParsedTemplateSection;
import net.officefloor.plugin.web.template.parse.ParsedTemplateSectionContent;
import net.officefloor.plugin.web.template.parse.PropertyParsedTemplateSectionContent;
import net.officefloor.plugin.web.template.parse.StaticParsedTemplateSectionContent;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.value.retrieve.ValueRetriever;
import net.officefloor.web.value.retrieve.ValueRetrieverSource;

/**
 * {@link ManagedFunction} to write the {@link ParsedTemplateSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * Property prefix to obtain the bean for the {@link ParsedTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = "bean.";

	/**
	 * Property prefix to obtain whether the link is to be secure.
	 */
	public static final String PROPERTY_LINK_SECURE_PREFIX = "link.secure.";

	/**
	 * Loads the {@link ManagedFunctionType} to write the
	 * {@link ParsedTemplateSection}.
	 * 
	 * @param section
	 *            {@link ParsedTemplateSection}.
	 * @param charset
	 *            {@link Charset} for the template.
	 * @param isTemplateSecure
	 *            Indicates if the template is to be secure.
	 * @param namespaceTypeBuilder
	 *            {@link FunctionNamespaceBuilder}.
	 * @param context
	 *            {@link ManagedFunctionSourceContext}.
	 * @return Listing of {@link ManagedFunction} names to handle
	 *         {@link ParsedTemplate} link requests.
	 * @throws Exception
	 *             If fails to prepare the template.
	 */
	public static String[] loadFunctionType(ParsedTemplateSection section, Charset charset, boolean isTemplateSecure,
			FunctionNamespaceBuilder namespaceTypeBuilder, ManagedFunctionSourceContext context) throws Exception {

		// Obtain the section and task name
		String sectionAndTaskName = section.getSectionName();

		// Set of link task names
		Set<String> linkTaskNames = new HashSet<String>();

		// Optional bean class
		Class<?> beanClass = getBeanClass(sectionAndTaskName, false, context);

		// Create the content writers for the section
		SectionWriterStruct writerStruct = createHttpTemplateWriters(section.getContent(), beanClass,
				sectionAndTaskName, linkTaskNames, charset, isTemplateSecure, context);

		// Determine if bean
		boolean isBean = (writerStruct.beanClass != null);

		// Create the function factory
		WebTemplateFunction function = new WebTemplateFunction(writerStruct.writers, isBean, charset);

		// Define the task to write the section
		ManagedFunctionTypeBuilder<Indexed, None> taskBuilder = namespaceTypeBuilder
				.addManagedFunctionType(sectionAndTaskName, function, Indexed.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
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
	 *            Indicates whether the {@link ParsedTemplate} is secure.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return <code>true</code> should the link be secure.
	 */
	public static boolean isLinkSecure(String linkName, boolean isTemplateSecure, SourceProperties properties) {

		// Determine if the link should be secure
		boolean isLinkSecure = Boolean.parseBoolean(
				properties.getProperty(PROPERTY_LINK_SECURE_PREFIX + linkName, String.valueOf(isTemplateSecure)));

		// Return whether secure
		return isLinkSecure;
	}

	/**
	 * Section {@link WebTemplateWriter} struct.
	 */
	private static class SectionWriterStruct {

		/**
		 * {@link WebTemplateWriter} instances.
		 */
		public final WebTemplateWriter[] writers;

		/**
		 * Bean class. <code>null</code> indicates no bean required.
		 */
		public final Class<?> beanClass;

		/**
		 * Initiate.
		 * 
		 * @param writers
		 *            {@link WebTemplateWriter} instances.
		 * @param beanClass
		 *            Bean class.
		 */
		public SectionWriterStruct(WebTemplateWriter[] writers, Class<?> beanClass) {
			this.writers = writers;
			this.beanClass = beanClass;
		}
	}

	/**
	 * Obtains the {@link SectionWriterStruct}.
	 * 
	 * @param contents
	 *            {@link ParsedTemplateSectionContent} instances.
	 * @param beanClass
	 *            Bean {@link Class}.
	 * @param sectionAndTaskName
	 *            Section and task name.
	 * @param linkTaskNames
	 *            List task names.
	 * @param charset
	 *            {@link Charset} for the template.
	 * @param isTemplateSecure
	 *            Indicates if the template is to be secure.
	 * @param context
	 *            {@link ManagedFunctionSourceContext}.
	 * @return {@link SectionWriterStruct}.
	 * @throws Exception
	 *             If fails to create the {@link SectionWriterStruct}.
	 */
	private static SectionWriterStruct createHttpTemplateWriters(ParsedTemplateSectionContent[] contents,
			Class<?> beanClass, String sectionAndTaskName, Set<String> linkTaskNames, Charset charset,
			boolean isTemplateSecure, ManagedFunctionSourceContext context) throws Exception {

		// Create the content writers for the section
		List<WebTemplateWriter> contentWriterList = new LinkedList<WebTemplateWriter>();
		ValueRetriever<Object> valueRetriever = null;
		for (ParsedTemplateSectionContent content : contents) {

			// Handle based on type
			if (content instanceof StaticParsedTemplateSectionContent) {
				// Add the static template writer
				StaticParsedTemplateSectionContent staticContent = (StaticParsedTemplateSectionContent) content;
				contentWriterList.add(new StaticWebTemplateWriter(staticContent, charset));

			} else if (content instanceof BeanParsedTemplateSectionContent) {
				// Add the bean template writer
				BeanParsedTemplateSectionContent beanContent = (BeanParsedTemplateSectionContent) content;

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
				Class<?> beanType = valueRetriever.getValueType(beanPropertyName);
				if (beanType == null) {
					throw new Exception(
							"Bean '" + beanPropertyName + "' can not be sourced from bean type " + beanClass.getName());
				}

				// Determine if an array of beans
				boolean isArray = false;
				if (beanType.isArray()) {
					isArray = true;
					beanType = beanType.getComponentType();
				}

				// Obtain the writers for the bean
				SectionWriterStruct beanStruct = createHttpTemplateWriters(beanContent.getContent(), beanType, null,
						linkTaskNames, charset, isTemplateSecure, context);

				// Add the content writer
				contentWriterList
						.add(new BeanWebTemplateWriter(beanContent, valueRetriever, isArray, beanStruct.writers));

			} else if (content instanceof PropertyParsedTemplateSectionContent) {
				// Add the property template writer
				PropertyParsedTemplateSectionContent propertyContent = (PropertyParsedTemplateSectionContent) content;

				// Ensure have bean class
				if (beanClass == null) {
					beanClass = getBeanClass(sectionAndTaskName, true, context);
				}

				// Ensure have the value loader for the bean
				if (valueRetriever == null) {
					valueRetriever = createValueRetriever(beanClass);
				}

				// Add the content writer
				contentWriterList.add(new PropertyWebTemplateWriter(propertyContent, valueRetriever, beanClass));

			} else if (content instanceof LinkParsedTemplateSectionContent) {
				// Add the link template writer
				LinkParsedTemplateSectionContent linkContent = (LinkParsedTemplateSectionContent) content;

				// Determine if the link is to be secure
				String linkName = linkContent.getName();
				boolean isLinkSecure = isLinkSecure(linkName, isTemplateSecure, context);

				// Add the content writer
				contentWriterList.add(new LinkWebTemplateWriter(linkContent, isLinkSecure));

				// Track the link tasks
				linkTaskNames.add(linkName);

			} else {
				// Unknown content
				throw new IllegalStateException("Unknown content type '" + content.getClass().getName());
			}
		}

		// Return the HTTP Template writers
		return new SectionWriterStruct(contentWriterList.toArray(new WebTemplateWriter[contentWriterList.size()]),
				beanClass);
	}

	/**
	 * Obtains the bean {@link Class}.
	 * 
	 * @param sectionAndTaskName
	 *            Section and task name.
	 * @param context
	 *            {@link ManagedFunctionSourceContext}.
	 * @return Bean {@link Class}.
	 */
	private static Class<?> getBeanClass(String sectionAndTaskName, boolean isRequired,
			ManagedFunctionSourceContext context) {

		// Obtain the bean class name
		String beanClassPropertyName = PROPERTY_BEAN_PREFIX + sectionAndTaskName;
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
	private static ValueRetriever<Object> createValueRetriever(Class<?> beanClass) throws Exception {

		// Obtain the value retriever
		ValueRetrieverSource source = new ValueRetrieverSource(true);
		ValueRetriever valueRetriever = source.sourceValueRetriever(beanClass);

		// Return the value retriever
		return valueRetriever;
	}

	/**
	 * {@link WebTemplateWriter} instances to write the content.
	 */
	private final WebTemplateWriter[] contentWriters;

	/**
	 * Flag indicating if a bean.
	 */
	private final boolean isBean;

	/**
	 * Default {@link Charset} for the template.
	 */
	private final Charset defaultCharset;

	/**
	 * Initiate.
	 * 
	 * @param contentWriters
	 *            {@link WebTemplateWriter} instances to write the content.
	 * @param isBean
	 *            Flag indicating if a bean.
	 * @param charset
	 *            Default {@link Charset} for the template.
	 */
	public WebTemplateFunction(WebTemplateWriter[] contentWriters, boolean isBean, Charset charset) {
		this.contentWriters = contentWriters;
		this.isBean = isBean;
		this.defaultCharset = charset;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws IOException {

		// Obtain the bean dependency
		Object bean;
		if (this.isBean) {
			// Obtain the bean
			bean = context.getObject(1);

			// No bean, no content
			if (bean == null) {
				return null;
			}

		} else {
			// No bean
			bean = null;
		}

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(0);

		// Obtain the writer
		HttpResponse response = connection.getResponse();
		ServerWriter writer = response.getEntityWriter();

		// Determine if using default charset
		boolean isDefaultCharset = (this.defaultCharset.name().equals(response.getContentCharset().name()));

		// Determine the template path
		String path = connection.getRequest().getUri();
		int endPathIndex = path.indexOf('?');
		if (endPathIndex >= 0) {
			path = path.substring(0, endPathIndex);
		}

		try {

			// Write the contents
			for (WebTemplateWriter contentWriter : this.contentWriters) {
				contentWriter.write(writer, isDefaultCharset, bean, connection, path);
			}

			// Flush contents
			writer.flush();

		} catch (IOException ex) {
			throw new HttpException(ex);
		}

		// Template written, nothing to return
		return null;
	}

}