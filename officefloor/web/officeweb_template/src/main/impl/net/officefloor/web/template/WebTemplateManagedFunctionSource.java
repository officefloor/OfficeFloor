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
package net.officefloor.web.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.web.template.parse.LinkParsedTemplateSectionContent;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.ParsedTemplateSectionContent;
import net.officefloor.web.template.parse.PropertyParsedTemplateSectionContent;
import net.officefloor.web.template.parse.WebTemplateParserImpl;

/**
 * {@link ManagedFunctionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateManagedFunctionSource extends AbstractManagedFunctionSource
		implements ManagedFunctionSourceService<WebTemplateManagedFunctionSource> {

	/**
	 * Property to specify the {@link ParsedTemplate} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.path";

	/**
	 * Property to obtain the raw {@link ParsedTemplate} content. This is tried
	 * if there is no template path.
	 */
	public static final String PROPERTY_TEMPLATE_CONTENT = "template.content";

	/**
	 * Property to indicate if the {@link ParsedTemplate} requires a secure
	 * {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_TEMPLATE_SECURE = "template.secure";

	/**
	 * Property to specify the {@link Charset} for reading in the template.
	 */
	public static final String PROPERTY_TEMPLATE_FILE_CHARSET = "template.file.charset";

	/**
	 * Property prefix to obtain whether a specific link requires a secure
	 * {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_LINK_SECURE_PREFIX = WebTemplateFunction.PROPERTY_LINK_SECURE_PREFIX;

	/**
	 * Property to specify the {@link Charset} for outputting the template.
	 */
	public static final String PROPERTY_CHARSET = "web.template.charset";

	/**
	 * Property prefix to obtain the bean for the {@link ParsedTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = WebTemplateFunction.PROPERTY_BEAN_PREFIX;

	/**
	 * Obtains the {@link ParsedTemplate}.
	 * 
	 * @param context
	 *            {@link SourceContext} providing details about the
	 *            {@link ParsedTemplate}.
	 * @return {@link ParsedTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link ParsedTemplate}.
	 */
	public static ParsedTemplate getParsedTemplate(SourceContext context) throws IOException {

		// Obtain the template content
		try (Reader content = getWebTemplateContent(context)) {

			// Obtain the template
			ParsedTemplate template = getWebTemplate(content);

			// Return the template
			return template;
		}
	}

	/**
	 * Obtains the raw {@link ParsedTemplate} content.
	 * 
	 * @param context
	 *            {@link SourceContext} providing details about the
	 *            {@link ParsedTemplate}.
	 * @return Raw {@link ParsedTemplate} content.
	 * @throws IOException
	 *             If fails to obtain the raw {@link ParsedTemplate} content.
	 */
	public static Reader getWebTemplateContent(SourceContext context) throws IOException {

		// Determine if content provided by property
		String templateContent = context.getProperty(PROPERTY_TEMPLATE_CONTENT, null);
		if (templateContent != null) {
			// Provided template content
			return new StringReader(templateContent);
		}

		// Not in property, so obtain details from file
		String templateFilePath = context.getProperty(PROPERTY_TEMPLATE_FILE);
		String templateFileCharsetName = context.getProperty(PROPERTY_TEMPLATE_FILE_CHARSET, null);
		Charset templateFileCharset = null;
		if (templateFileCharsetName != null) {
			templateFileCharset = Charset.forName(templateFileCharsetName);
		}

		// Obtain the configuration
		InputStream configuration = context.getResource(templateFilePath);

		// Return the reader to the template content
		return (templateFileCharset == null ? new InputStreamReader(configuration)
				: new InputStreamReader(configuration, templateFileCharset));
	}

	/**
	 * Obtains the {@link ParsedTemplate}.
	 * 
	 * @param templateContent
	 *            Raw {@link ParsedTemplate} content.
	 * @return {@link ParsedTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link ParsedTemplate}.
	 */
	public static ParsedTemplate getWebTemplate(Reader templateContent) throws IOException {

		// Parse the template
		ParsedTemplate template = new WebTemplateParserImpl(templateContent).parse();

		// Return the template
		return template;
	}

	/**
	 * Determines if the {@link ParsedTemplateSection} requires a bean.
	 * 
	 * @param section
	 *            {@link ParsedTemplateSection}.
	 * @return <code>true</code> if the {@link ParsedTemplateSection} requires a
	 *         bean.
	 */
	public static boolean isParsedTemplateSectionRequireBean(ParsedTemplateSection section) {

		// Determine if contains reference content
		for (ParsedTemplateSectionContent content : section.getContent()) {
			if ((content instanceof PropertyParsedTemplateSectionContent)
					|| (content instanceof BeanParsedTemplateSectionContent)) {
				// Section contains property/bean reference, so requires bean
				return true;
			}
		}

		// No reference to property/bean, so does not require bean
		return false;
	}

	/**
	 * Determines if the {@link WebTemplate} is secure.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return <code>true</code> should the {@link ParsedTemplate} be secure.
	 */
	public static boolean isWebTemplateSecure(SourceProperties properties) {

		// Determine if template is secure
		boolean isTemplateSecure = Boolean
				.valueOf(properties.getProperty(PROPERTY_TEMPLATE_SECURE, String.valueOf(false)));

		// Return whether secure
		return isTemplateSecure;
	}

	/**
	 * Obtains the link names for the {@link ParsedTemplate}.
	 * 
	 * @param template
	 *            {@link ParsedTemplate}.
	 * @return Link names.
	 */
	public static String[] getParsedTemplateLinkNames(ParsedTemplate template) {

		// Obtain the listing of link names
		List<String> linkNames = new LinkedList<String>();
		for (ParsedTemplateSection section : template.getSections()) {
			loadLinkNames(section.getContent(), linkNames);
		}

		// Return the link names
		return linkNames.toArray(new String[linkNames.size()]);
	}

	/**
	 * Loads the link names from the {@link ParsedTemplateSectionContent}
	 * instances.
	 * 
	 * @param contents
	 *            {@link ParsedTemplateSectionContent} instances.
	 * @param linkNames
	 *            {@link List} to receive the unique link names.
	 */
	private static void loadLinkNames(ParsedTemplateSectionContent[] contents, List<String> linkNames) {

		// Interrogate contents for links
		for (ParsedTemplateSectionContent content : contents) {

			// Add the link
			if (content instanceof LinkParsedTemplateSectionContent) {
				LinkParsedTemplateSectionContent link = (LinkParsedTemplateSectionContent) content;

				// Obtain the link name
				String linkName = link.getName();

				// Add the link name
				if (!linkNames.contains(linkName)) {
					linkNames.add(linkName);
				}
			}

			// Recursively check bean content for links
			if (content instanceof BeanParsedTemplateSectionContent) {
				BeanParsedTemplateSectionContent beanContent = (BeanParsedTemplateSectionContent) content;
				loadLinkNames(beanContent.getContent(), linkNames);
			}
		}
	}

	/*
	 * ================== ManagedFunctionSourceService ==================
	 */

	@Override
	public String getManagedFunctionSourceAlias() {
		return "HTTP_TEMPLATE";
	}

	@Override
	public Class<WebTemplateManagedFunctionSource> getManagedFunctionSourceClass() {
		return WebTemplateManagedFunctionSource.class;
	}

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_FILE, "Template");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the template
		ParsedTemplate template = getParsedTemplate(context);

		// Obtain the details of the template
		Charset charset = null;
		String charsetName = context.getProperty(PROPERTY_CHARSET, null);
		if (!CompileUtil.isBlank(charsetName)) {
			charset = Charset.forName(charsetName);
		}

		// Obtain whether the template is secure
		boolean isTemplateSecure = isWebTemplateSecure(context);

		// Define the functions
		for (ParsedTemplateSection section : template.getSections()) {

			// Load the function to write the section
			WebTemplateFunction.loadFunctionType(section, charset, isTemplateSecure, namespaceTypeBuilder, context);
		}
	}

}