/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.officefloor.compile.WorkSourceService;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactoryImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateParserImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.ReferenceHttpTemplateSectionContent;

/**
 * {@link WorkSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSource extends
		AbstractWorkSource<HttpTemplateWork> implements
		WorkSourceService<HttpTemplateWork, HttpTemplateWorkSource> {

	/**
	 * Property to specify the {@link HttpTemplate} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.path";

	/**
	 * Property to obtain the raw {@link HttpTemplate} content. This is tried if
	 * there is no template path.
	 */
	public static final String PROPERTY_TEMPLATE_CONTENT = "template.content";

	/**
	 * Property to specify the <code>Content-Type</code> for the template.
	 */
	public static final String PROPERTY_CONTENT_TYPE = "content.type";

	/**
	 * Property to specify the {@link Charset} for the template.
	 */
	public static final String PROPERTY_CHARSET = "charset";

	/**
	 * Property prefix to obtain the bean for the {@link HttpTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = HttpTemplateTask.PROPERTY_BEAN_PREFIX;

	/**
	 * Extension on a link URL.
	 */
	public static final String LINK_URL_EXTENSION = ".task";

	/**
	 * Registered {@link RawHttpTemplateLoader} instances.
	 */
	private static final List<RawHttpTemplateLoader> loaders = new LinkedList<RawHttpTemplateLoader>();

	/**
	 * Registers the {@link RawHttpTemplateLoader}.
	 * 
	 * @param rawHttpTemplateLoader
	 *            {@link RawHttpTemplateLoader}.
	 */
	public static void registerRawHttpTemplateLoader(
			RawHttpTemplateLoader rawHttpTemplateLoader) {
		synchronized (loaders) {
			loaders.add(rawHttpTemplateLoader);
		}
	}

	/**
	 * <p>
	 * Unregisters all the {@link RawHttpTemplateLoader} instances.
	 * <p>
	 * This is typically only made available to allow resetting content for
	 * testing.
	 */
	public static void unregisterAllRawHttpTemplateLoaders() {
		synchronized (loaders) {
			loaders.clear();
		}
	}

	/**
	 * Obtains the {@link HttpTemplate}.
	 * 
	 * @param properties
	 *            {@link SourceProperties} providing details about the
	 *            {@link HttpTemplate}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link HttpTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpTemplate}.
	 */
	public static HttpTemplate getHttpTemplate(SourceProperties properties,
			ClassLoader classLoader) throws IOException {

		// Obtain the template content
		Reader content = getHttpTemplateContent(properties, classLoader);

		// Obtain the template
		HttpTemplate template = getHttpTemplate(content);

		// Template content read
		content.close();

		// Return the template
		return template;
	}

	/**
	 * Obtains the raw {@link HttpTemplate} content.
	 * 
	 * @param properties
	 *            {@link SourceProperties} providing details about the
	 *            {@link HttpTemplate}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return Raw {@link HttpTemplate} content.
	 * @throws IOException
	 *             If fails to obtain the raw {@link HttpTemplate} content.
	 */
	public static Reader getHttpTemplateContent(SourceProperties properties,
			ClassLoader classLoader) throws IOException {

		// Determine if content provided by property
		String templateContent = properties.getProperty(
				PROPERTY_TEMPLATE_CONTENT, null);
		if (templateContent != null) {
			// Provided template content
			return new StringReader(templateContent);
		}

		// Not in property, so obtain details from file
		String templateFilePath = properties
				.getProperty(PROPERTY_TEMPLATE_FILE);
		Charset charset = getCharset(properties);

		// Try the Raw HTTP Template Loaders first
		synchronized (loaders) {
			for (RawHttpTemplateLoader loader : loaders) {
				Reader content = loader.loadRawHttpTemplate(templateFilePath,
						charset);
				if (content != null) {
					return content; // found content
				}
			}
		}

		// Last attempt on the class path
		InputStream configuration = classLoader
				.getResourceAsStream(templateFilePath);
		if (configuration == null) {
			throw new FileNotFoundException("Can not find template '"
					+ templateFilePath + "'");
		}

		// Return the reader to the template content
		return new InputStreamReader(configuration, charset);
	}

	/**
	 * Obtains the {@link HttpTemplate}.
	 * 
	 * @param templateContent
	 *            Raw {@link HttpTemplate} content.
	 * @return {@link HttpTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpTemplate}.
	 */
	public static HttpTemplate getHttpTemplate(Reader templateContent)
			throws IOException {

		// Parse the template
		HttpTemplate template = new HttpTemplateParserImpl()
				.parse(templateContent);

		// Return the template
		return template;
	}

	/**
	 * Determines if the {@link HttpTemplateSection} requires a bean.
	 * 
	 * @param section
	 *            {@link HttpTemplateSection}.
	 * @return <code>true</code> if the {@link HttpTemplateSection} requires a
	 *         bean.
	 */
	public static boolean isHttpTemplateSectionRequireBean(
			HttpTemplateSection section) {

		// Determine if contains reference content
		for (HttpTemplateSectionContent content : section.getContent()) {
			if (content instanceof ReferenceHttpTemplateSectionContent) {
				// Section contains reference content, so requires bean
				return true;
			}
		}

		// No reference content, so does not require bean
		return false;
	}

	/**
	 * Obtains the link names for the {@link HttpTemplate}.
	 * 
	 * @param template
	 *            {@link HttpTemplate}.
	 * @return Link names.
	 */
	public static String[] getHttpTemplateLinkNames(HttpTemplate template) {

		// Obtain the listing of link names
		List<String> linkNames = new LinkedList<String>();
		for (HttpTemplateSection section : template.getSections()) {
			for (HttpTemplateSectionContent content : section.getContent()) {
				if (content instanceof LinkHttpTemplateSectionContent) {
					LinkHttpTemplateSectionContent link = (LinkHttpTemplateSectionContent) content;

					// Obtain the link name
					String linkName = link.getName();

					// Add the link name
					if (!linkNames.contains(linkName)) {
						linkNames.add(linkName);
					}
				}
			}
		}

		// Return the link names
		return linkNames.toArray(new String[linkNames.size()]);
	}

	/**
	 * Obtains the {@link Charset} from the {@link Properties}.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return {@link Charset}.
	 */
	private static Charset getCharset(SourceProperties properties) {

		// Obtain the charset
		String charsetName = properties.getProperty(PROPERTY_CHARSET, null);
		Charset charset = (charsetName != null ? Charset.forName(charsetName)
				: Charset.defaultCharset());

		// Return the charset
		return charset;
	}

	/*
	 * ====================== WorkSourceService ===========================
	 */

	@Override
	public String getWorkSourceAlias() {
		return "HTTP_TEMPLATE";
	}

	@Override
	public Class<HttpTemplateWorkSource> getWorkSourceClass() {
		return HttpTemplateWorkSource.class;
	}

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_FILE, "template");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpTemplateWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the template
		HttpTemplate template = getHttpTemplate(context,
				context.getClassLoader());

		// Obtain the details of the template
		String contentType = context.getProperty(PROPERTY_CONTENT_TYPE,
				"text/html");
		Charset charset = getCharset(context);

		// Create the writer factory
		HttpResponseWriterFactory writerFactory = new HttpResponseWriterFactoryImpl();

		// Define the work factory
		workTypeBuilder.setWorkFactory(new HttpTemplateWork());

		// Define the tasks
		Set<String> linkNameSet = new HashSet<String>();
		for (HttpTemplateSection section : template.getSections()) {

			// Load the task to write the section
			String[] linkNames = HttpTemplateTask.loadTaskType(section,
					contentType, charset, writerFactory, workTypeBuilder,
					context);

			// Keep track of the unique set of link names
			linkNameSet.addAll(Arrays.asList(linkNames));
		}

		// Add the request handler tasks in order
		String[] requestHandlerTaskNames = linkNameSet.toArray(new String[0]);
		Arrays.sort(requestHandlerTaskNames);
		for (String requestHandlerTaskName : requestHandlerTaskNames) {

			// Add request handler task
			TaskTypeBuilder<Indexed, None> task = workTypeBuilder.addTaskType(
					requestHandlerTaskName, new RequestHandlerTask(),
					Indexed.class, None.class);

			// Specify differentiator to enable finding this task
			task.setDifferentiator(new HttpTemplateRequestHandlerDifferentiator());
		}
	}

}