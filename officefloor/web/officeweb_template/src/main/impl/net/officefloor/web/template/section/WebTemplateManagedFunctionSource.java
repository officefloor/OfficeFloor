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
package net.officefloor.web.template.section;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.WebTemplateParser;
import net.officefloor.web.template.section.WebTemplateSectionSource.SectionWriterStruct;

/**
 * {@link ManagedFunctionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name to obtain the template content.
	 */
	public static final String PROPERTY_TEMPLATE_CONTENT = "template.content";

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_CONTENT, "Template Content");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the template
		String content = context.getProperty(PROPERTY_TEMPLATE_CONTENT);
		ParsedTemplate template = WebTemplateParser.parse(new StringReader(content));

		// Obtain the details of the template
		Charset charset = WebTemplateSectionSource.getWebTemplateRenderCharset(context);

		// Obtain whether the template is secure
		boolean isTemplateSecure = WebTemplateSectionSource.isTemplateSecure(context);

		// Define the functions
		for (ParsedTemplateSection section : template.getSections()) {

			// Obtain the section and function name
			String sectionAndFunctionName = section.getSectionName();

			// Set of link function names
			Set<String> linkFunctionNames = new HashSet<String>();

			// Optional bean class
			Class<?> beanClass = WebTemplateSectionSource.getBeanClass(sectionAndFunctionName, false, context);

			// Create the content writers for the section
			SectionWriterStruct writerStruct = WebTemplateSectionSource.createWebTemplateWriters(section.getContent(),
					beanClass, sectionAndFunctionName, linkFunctionNames, charset, isTemplateSecure, context);

			// Determine if bean
			boolean isBean = (writerStruct.beanClass != null);

			// Create the function factory
			WebTemplateFunction function = new WebTemplateFunction(writerStruct.writers, isBean, charset);

			// Define the function to write the section
			ManagedFunctionTypeBuilder<Indexed, None> functionBuilder = namespaceTypeBuilder
					.addManagedFunctionType(sectionAndFunctionName, function, Indexed.class, None.class);
			functionBuilder.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
			if (isBean) {
				functionBuilder.addObject(writerStruct.beanClass).setLabel("OBJECT");
			}
			functionBuilder.addEscalation(IOException.class);
		}
	}

}