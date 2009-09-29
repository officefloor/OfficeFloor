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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactoryImpl;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplate;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateParserImpl;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplateSection;

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

		// Obtain the details of the template
		String templateFilePath = context.getProperty(PROPERTY_TEMPLATE_FILE);
		String contentType = context.getProperty(PROPERTY_CONTENT_TYPE,
				"text/html");
		String charsetName = context.getProperty(PROPERTY_CHARSET, null);
		Charset charset = (charsetName != null ? Charset.forName(charsetName)
				: Charset.defaultCharset());

		// Obtain the template configuration
		InputStream configuration = context.getClassLoader()
				.getResourceAsStream(templateFilePath);
		if (configuration == null) {
			throw new FileNotFoundException("Can not find template '"
					+ templateFilePath + "'");
		}

		// Parse the template
		HttpTemplate template = new HttpTemplateParserImpl()
				.parse(new InputStreamReader(configuration, charset));
		configuration.close();

		// Create the writer factory
		HttpResponseWriterFactory writerFactory = new HttpResponseWriterFactoryImpl();

		// Define the work factory
		workTypeBuilder.setWorkFactory(new HttpTemplateWork());

		// Define the tasks
		for (HttpTemplateSection section : template.getSections()) {

			// Load the task to write the section
			HttpTemplateTask.loadTaskType(section, contentType, charset,
					writerFactory, workTypeBuilder, context);
		}
	}

}