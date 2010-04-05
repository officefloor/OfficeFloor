/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactoryImpl;
import net.officefloor.plugin.socket.server.http.template.RequestHandlerTask.RequestHandlerIdentifier;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplate;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateParserImpl;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.socket.server.http.template.parse.ReferenceHttpTemplateSectionContent;

/**
 * {@link WorkSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSource extends
		AbstractWorkSource<HttpTemplateWork> {

	/**
	 * Property to specify the {@link HttpTemplate} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.path";

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
	 * Obtains the {@link HttpTemplate}.
	 * 
	 * @param properties
	 *            {@link Properties} providing details about the
	 *            {@link HttpTemplate}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link HttpTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpTemplate}.
	 */
	public static HttpTemplate getHttpTemplate(Properties properties,
			ClassLoader classLoader) throws IOException {

		// Obtain the details of the template
		String templateFilePath = properties
				.getProperty(PROPERTY_TEMPLATE_FILE);
		Charset charset = getCharset(properties);

		// Obtain the template configuration
		InputStream configuration = classLoader
				.getResourceAsStream(templateFilePath);
		if (configuration == null) {
			throw new FileNotFoundException("Can not find template '"
					+ templateFilePath + "'");
		}

		// Parse the template
		HttpTemplate template = new HttpTemplateParserImpl()
				.parse(new InputStreamReader(configuration, charset));
		configuration.close();

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
	 * Obtains the {@link Charset} from the {@link Properties}.
	 * 
	 * @param properties
	 *            {@link Properties}.
	 * @return {@link Charset}.
	 */
	private static Charset getCharset(Properties properties) {

		// Obtain the charset
		String charsetName = properties.getProperty(PROPERTY_CHARSET, null);
		Charset charset = (charsetName != null ? Charset.forName(charsetName)
				: Charset.defaultCharset());

		// Return the charset
		return charset;
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
		Properties properties = context.getProperties();
		HttpTemplate template = getHttpTemplate(properties, context
				.getClassLoader());

		// Obtain the details of the template
		String contentType = context.getProperty(PROPERTY_CONTENT_TYPE,
				"text/html");
		Charset charset = getCharset(properties);

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

			// Add marker object to allow it to be found for routing.
			// (Should be configured as parameter and may be passed null)
			task.addObject(RequestHandlerIdentifier.class);
		}
	}

}