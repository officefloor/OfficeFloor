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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.impl.properties.PropertiesUtil;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.web.template.NotRenderTemplateAfter;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateExtensionBuilder;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.extension.WebTemplateExtensionContext;
import net.officefloor.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.web.template.parse.LinkParsedTemplateSectionContent;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.ParsedTemplateSectionContent;
import net.officefloor.web.template.parse.PropertyParsedTemplateSectionContent;
import net.officefloor.web.template.parse.StaticParsedTemplateSectionContent;
import net.officefloor.web.template.parse.WebTemplateParser;
import net.officefloor.web.template.section.WebTemplateArrayIteratorFunction.DependencyKeys;
import net.officefloor.web.template.section.WebTemplateArrayIteratorFunction.FlowKeys;
import net.officefloor.web.template.section.WebTemplateInitialFunction.Flows;
import net.officefloor.web.template.section.WebTemplateInitialFunction.WebTemplateInitialDependencies;
import net.officefloor.web.value.retrieve.ValueRetriever;
import net.officefloor.web.value.retrieve.ValueRetrieverSource;

/**
 * {@link SectionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class WebTemplateSectionSource extends ClassSectionSource {

	/**
	 * <p>
	 * Class to use if no class specified.
	 * <p>
	 * Must have a public method for {@link ClassSectionSource}.
	 */
	public static final class NoLogicClass {
		public void notIncludedInput() {
		}
	}

	/**
	 * Name of the {@link SectionInput} for rendering this {@link ParsedTemplate}.
	 */
	public static final String RENDER_TEMPLATE_INPUT_NAME = "renderTemplate";

	/**
	 * Name of the {@link SectionOutput} for redirect to the {@link WebTemplate}.
	 */
	public static final String REDIRECT_TEMPLATE_OUTPUT_NAME = "redirectToTemplate";

	/**
	 * Name of the {@link SectionOutput} for flow after completion of rending the
	 * {@link ParsedTemplate}.
	 */
	public static final String ON_COMPLETION_OUTPUT_NAME = "output";

	/**
	 * Prefix on a {@link ParsedTemplateSection} name to indicate it is an override
	 * section.
	 */
	public static final String OVERRIDE_SECTION_PREFIX = ":";

	/**
	 * Name of {@link Property} for the number of inherited templates.
	 */
	public static final String PROPERTY_INHERITED_TEMPLATES_COUNT = "template.inherit.count";

	/**
	 * Name of {@link Property} to obtain the raw {@link ParsedTemplate} content.
	 */
	public static final String PROPERTY_TEMPLATE_CONTENT = "template.content";

	/**
	 * Name of {@link Property} providing the location of the {@link WebTemplate}
	 * content.
	 */
	public static final String PROPERTY_TEMPLATE_LOCATION = "template.location";

	/**
	 * Name of {@link Property} providing the {@link Charset} to read in the
	 * {@link WebTemplate} content at the configured
	 * {@link #PROPERTY_TEMPLATE_LOCATION}.
	 */
	public static final String PROPERTY_TEMPLATE_LOCATION_CHARSET = "template.location.charset";

	/**
	 * Name of {@link Property} for the {@link Class} providing the backing logic to
	 * the template.
	 */
	public static final String PROPERTY_CLASS_NAME = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME;

	/**
	 * <p>
	 * Name of {@link Property} to indicate if this {@link WebTemplate} contains
	 * {@link HttpPathParameter} instances (dynamic path).
	 * <p>
	 * Note that specifying the {@link HttpInputPath} overrides this configured
	 * value.
	 */
	public static final String PROPERTY_IS_PATH_PARAMETERS = "template.is.path.parameters";

	/**
	 * Name of {@link Property} for the {@link Method} name on the logic
	 * {@link Class} that will return an object containing the values for the path
	 * parameters in redirecting to this {@link WebTemplate}.
	 */
	public static final String PROPERTY_REDIRECT_VALUES_FUNCTION = "template.redirect.values.function";

	/**
	 * {@link Property} prefix to obtain the bean for the
	 * {@link ParsedTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = "bean.";

	/**
	 * Name of {@link Property} to indicate if the {@link ParsedTemplate} requires a
	 * secure {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_TEMPLATE_SECURE = "template.secure";

	/**
	 * {@link Property} prefix to obtain whether the link is to be secure.
	 */
	public static final String PROPERTY_LINK_SECURE_PREFIX = "template.link.secure.";

	/**
	 * Name of {@link Property} to obtain the link separator.
	 */
	public static final String PROPERTY_LINK_SEPARATOR = "template.link.separator";

	/**
	 * Default link separator {@link Character}.
	 */
	private static final char DEFAULT_LINK_SEPARATOR = '+';

	/**
	 * Name of {@link Property} for the <code>Content-Type</code> of the
	 * {@link ParsedTemplate}.
	 */
	public static final String PROPERTY_CONTENT_TYPE = "template.content.type";

	/**
	 * Name of {@link Property} for the {@link Charset} of the
	 * {@link ParsedTemplate}.
	 */
	public static final String PROPERTY_CHARSET = "template.charset";

	/**
	 * Obtains the {@link WebTemplate} content.
	 * 
	 * @param inheritanceIndex
	 *            Index within the inheritance. <code>-1</code> for this
	 *            {@link WebTemplate}.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param context
	 *            {@link SourceContext}.
	 * @return {@link WebTemplate} content.
	 */
	private static String getTemplateContent(int inheritanceIndex, SectionDesigner designer, SourceContext context) {

		// Calculate the inheritance suffix
		String inheritanceSuffix = (inheritanceIndex == -1 ? "" : "." + inheritanceIndex);

		// Obtain as content first
		String content = context.getProperty(PROPERTY_TEMPLATE_CONTENT + inheritanceSuffix, null);
		if (content != null) {
			return content;
		}

		// Obtain as location (requiring location to be configured)
		String location = context.getProperty(PROPERTY_TEMPLATE_LOCATION + inheritanceSuffix);
		String locationCharset = context.getProperty(PROPERTY_TEMPLATE_LOCATION_CHARSET + inheritanceSuffix, null);
		InputStream locationStream = context.getResource(location);
		StringWriter buffer = new StringWriter();
		try (Reader locationReader = (locationCharset == null ? new InputStreamReader(locationStream)
				: new InputStreamReader(locationStream, Charset.forName(locationCharset)))) {
			for (int character = locationReader.read(); character != -1; character = locationReader.read()) {
				buffer.write(character);
			}
		} catch (IOException ex) {
			// Indicate failed to read in template
			throw designer.addIssue("Failed to read in template at location '" + location + "' with charset '"
					+ (locationCharset == null ? "<default>" : locationCharset + ")"), ex);
		}
		return buffer.toString();
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
	private static boolean isLinkSecure(String linkName, boolean isTemplateSecure, SourceProperties properties) {

		// Determine if the link should be secure
		boolean isLinkSecure = Boolean.parseBoolean(
				properties.getProperty(PROPERTY_LINK_SECURE_PREFIX + linkName, String.valueOf(isTemplateSecure)));

		// Return whether secure
		return isLinkSecure;
	}

	/**
	 * Obtains the link names for the {@link ParsedTemplate}.
	 * 
	 * @param template
	 *            {@link ParsedTemplate}.
	 * @return {@link ParsedLink} instances.
	 */
	private static ParsedLink[] getParsedTemplateLinkNames(ParsedTemplate template) {

		// Obtain the listing of link names
		List<ParsedLink> linkNames = new LinkedList<ParsedLink>();
		for (ParsedTemplateSection section : template.getSections()) {
			loadLinkNames(section.getContent(), linkNames);
		}

		// Return the links
		return linkNames.toArray(new ParsedLink[linkNames.size()]);
	}

	/**
	 * Loads the link names from the {@link ParsedTemplateSectionContent} instances.
	 * 
	 * @param contents
	 *            {@link ParsedTemplateSectionContent} instances.
	 * @param links
	 *            {@link ParsedLink} listing to receive the unique links.
	 */
	private static void loadLinkNames(ParsedTemplateSectionContent[] contents, List<ParsedLink> links) {

		// Interrogate contents for links
		for (ParsedTemplateSectionContent content : contents) {

			// Add the link
			if (content instanceof LinkParsedTemplateSectionContent) {
				LinkParsedTemplateSectionContent link = (LinkParsedTemplateSectionContent) content;

				// Obtain the link name
				String linkName = link.getName();
				List<String> linkHttpMethodNames = new LinkedList<>();
				if (linkName.contains(":")) {
					String[] parts = linkName.split(":");

					// Link follows methods
					linkName = parts[1];

					// Supported methods listed first
					for (String methodName : parts[0].split(",")) {
						linkHttpMethodNames.add(methodName);
					}
				}

				// Add the link
				ParsedLink parsedLink = null;
				FOUND_LINK: for (ParsedLink checkLink : links) {
					if (checkLink.linkName.equals(linkName)) {
						parsedLink = checkLink;
						break FOUND_LINK;
					}
				}
				if (parsedLink == null) {
					parsedLink = new ParsedLink(linkName);
					links.add(parsedLink);
				}

				// Add the methods to the link
				for (String linkMethod : linkHttpMethodNames) {
					if (!(parsedLink.httpMethodNames.contains(linkMethod))) {
						parsedLink.httpMethodNames.add(linkMethod);
					}
				}
			}

			// Recursively check bean content for links
			if (content instanceof BeanParsedTemplateSectionContent) {
				BeanParsedTemplateSectionContent beanContent = (BeanParsedTemplateSectionContent) content;
				loadLinkNames(beanContent.getContent(), links);
			}
		}
	}

	/**
	 * Obtains the {@link ParsedTemplateSection} name (possibly removing override
	 * prefix).
	 * 
	 * @param sectionName
	 *            {@link ParsedTemplateSection} raw name.
	 * @return {@link ParsedTemplateSection} name.
	 */
	private static String getParsedTemplateSectionName(String sectionName) {
		return sectionName.startsWith(OVERRIDE_SECTION_PREFIX) ? sectionName.substring(OVERRIDE_SECTION_PREFIX.length())
				: sectionName;
	}

	/**
	 * Creates the {@link ManagedFunction} key from the {@link ManagedFunction}
	 * name.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return Key for the {@link ManagedFunction}.
	 */
	private static String createFunctionKey(String functionName) {
		// Provide name in upper case to avoid case sensitivity
		return functionName.toUpperCase();
	}

	/**
	 * Determine if the section class is stateful - annotated with
	 * {@link HttpSessionStateful}.
	 * 
	 * @param sectionClass
	 *            Section class.
	 * @return <code>true</code> if stateful.
	 */
	private static boolean isHttpSessionStateful(Class<?> sectionClass) {

		// Determine if stateful
		boolean isStateful = sectionClass.isAnnotationPresent(HttpSessionStateful.class);

		// Return indicating if stateful
		return isStateful;
	}

	/**
	 * {@link Class} providing the logic for the HTTP template - also the
	 * {@link Class} for the {@link ClassSectionSource}.
	 */
	private Class<?> sectionClass = null;

	/**
	 * {@link SectionManagedObject} for the section object.
	 */
	private SectionManagedObject sectionClassManagedObject = null;

	/**
	 * {@link TemplateClassFunction} for the section {@link Class} method by its
	 * name.
	 */
	private final Map<String, TemplateClassFunction> sectionClassMethodFunctionsByName = new HashMap<>();

	/**
	 * Listing of the {@link TemplateFlowLink} instances.
	 */
	private final List<TemplateFlowLink> flowLinks = new LinkedList<>();

	/**
	 * {@link HttpInputPath}.
	 */
	private HttpInputPath inputPath;

	/**
	 * {@link WebTemplateExtensionBuilderImpl} instances.
	 */
	private List<WebTemplateExtensionBuilderImpl> extensions = new LinkedList<>();

	/**
	 * Specifies the {@link HttpInputPath}.
	 * 
	 * @param inputPath
	 *            {@link HttpInputPath}.
	 */
	public void setHttpInputPath(HttpInputPath inputPath) {
		this.inputPath = inputPath;
	}

	/**
	 * Adds a {@link WebTemplateExtensionBuilder}.
	 * 
	 * @param extension
	 *            {@link WebTemplateExtension}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @return {@link WebTemplateExtensionBuilder}.
	 */
	public WebTemplateExtensionBuilder addWebTemplateExtension(WebTemplateExtension extension,
			PropertyList propertyList) {
		WebTemplateExtensionBuilderImpl extensionBuilder = new WebTemplateExtensionBuilderImpl(extension, propertyList);
		this.extensions.add(extensionBuilder);
		return extensionBuilder;
	}

	/*
	 * ===================== SectionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the section class
		String sectionClassName = context.getProperty(PROPERTY_CLASS_NAME, null);
		boolean isLogicClass = true;
		if (CompileUtil.isBlank(sectionClassName)) {
			// Use the no logic class
			sectionClassName = NoLogicClass.class.getName();
			isLogicClass = false; // No logic class
		}

		// Load the section class functions
		this.sectionClass = context.loadClass(sectionClassName);
		super.sourceSection(designer, context);

		// Obtain the template path (for logging details)
		String templatePath = context.getSectionLocation();

		// Calculate inheritance hierarchy of templates (with current as last)
		int inheritedTemplatesCount = Integer
				.parseInt(context.getProperty(PROPERTY_INHERITED_TEMPLATES_COUNT, String.valueOf(0)));
		String[] templateInheritanceHierarchy = new String[inheritedTemplatesCount + 1];
		for (int inheritanceIndex = 0; inheritanceIndex < inheritedTemplatesCount; inheritanceIndex++) {
			templateInheritanceHierarchy[inheritanceIndex] = getTemplateContent(inheritanceIndex, designer, context);
		}
		templateInheritanceHierarchy[templateInheritanceHierarchy.length - 1] = getTemplateContent(-1, designer,
				context);

		// Obtain the template for the highest ancestor in inheritance hierarchy
		ParsedTemplate highestAncestorTemplate = WebTemplateParser
				.parse(new StringReader(templateInheritanceHierarchy[0]));

		/*
		 * Keep track of all link names. This is to allow inherited links to be known
		 * even if they do not end up in the resulting inherited template (i.e.
		 * containing sections have been overridden).
		 */
		Set<String> knownLinkNames = new HashSet<String>();
		for (ParsedLink link : getParsedTemplateLinkNames(highestAncestorTemplate)) {
			knownLinkNames.add(link.linkName);
		}

		// Filter the comments from the parsed template sections
		Function<ParsedTemplateSection[], ParsedTemplateSection[]> filterCommentParsedTemplateSections = (
				filtering) -> {
			// Filter comment sections
			List<ParsedTemplateSection> filteredSections = new LinkedList<ParsedTemplateSection>();
			for (ParsedTemplateSection section : filtering) {

				// Ignore comment section
				if ("!".equals(section.getSectionName())) {
					continue;
				}

				// Include the section
				filteredSections.add(section);
			}

			// Return the filtered sections
			return filteredSections.toArray(new ParsedTemplateSection[filteredSections.size()]);
		};

		// Undertake inheritance of the template (first does not inherit)
		ParsedTemplateSection[] sections = highestAncestorTemplate.getSections();
		sections = filterCommentParsedTemplateSections.apply(sections);
		for (int i = 1; i < templateInheritanceHierarchy.length; i++) {

			// Obtain the child sections
			ParsedTemplate childTemplate = WebTemplateParser.parse(new StringReader(templateInheritanceHierarchy[i]));
			ParsedTemplateSection[] childSections = filterCommentParsedTemplateSections
					.apply(childTemplate.getSections());

			// Add the child link names
			for (ParsedLink link : getParsedTemplateLinkNames(childTemplate)) {
				knownLinkNames.add(link.linkName);
			}

			// Create the listing of sections for overriding
			Map<String, List<ParsedTemplateSection>> overrideSections = new HashMap<String, List<ParsedTemplateSection>>();
			String overrideSectionName = null;
			List<ParsedTemplateSection> overrideSectionList = new LinkedList<ParsedTemplateSection>();
			boolean isFirstSection = true;
			for (ParsedTemplateSection section : childSections) {

				// Obtain the section name
				String sectionName = section.getSectionName();

				// Determine if override section
				if (sectionName.startsWith(OVERRIDE_SECTION_PREFIX)) {
					// New override section
					overrideSectionName = getParsedTemplateSectionName(sectionName);
					overrideSectionList = new LinkedList<ParsedTemplateSection>();
					overrideSections.put(overrideSectionName, overrideSectionList);

				} else {
					// Determine if invalid introduced section
					if (overrideSectionName == null) {

						// Invalid introduced if not the default first section
						if (!((isFirstSection) && (WebTemplateParser.DEFAULT_FIRST_SECTION_NAME.equals(sectionName)))) {
							// Invalid introduced section
							throw designer.addIssue("Section '" + sectionName
									+ "' can not be introduced, as no previous override section (section prefixed with '"
									+ OVERRIDE_SECTION_PREFIX + "') to identify where to inherit");
						}
					}
				}

				// Include the section
				overrideSectionList.add(section);
				isFirstSection = false; // no longer first section
			}

			// Obtain the names of all parent sections
			Set<String> parentSectionNames = new HashSet<String>();
			for (ParsedTemplateSection parentSection : sections) {
				parentSectionNames.add(getParsedTemplateSectionName(parentSection.getSectionName()));
			}

			// Create the listing sections from inheritance
			List<ParsedTemplateSection> inheritanceSections = new LinkedList<ParsedTemplateSection>();
			for (ParsedTemplateSection parentSection : sections) {

				// Obtain the parent section name
				String parentSectionName = getParsedTemplateSectionName(parentSection.getSectionName());

				// Determine if overriding parent
				List<ParsedTemplateSection> overridingSections = overrideSections.remove(parentSectionName);
				if (overridingSections == null) {
					// Parent section not overridden, so include
					inheritanceSections.add(parentSection);

				} else {
					// Overridden, so include override and introduced sections
					boolean isIntroducedSection = false; // first is override
					for (ParsedTemplateSection overrideSection : overridingSections) {

						// Determine if introduced section already exists
						String introducedSectionName = getParsedTemplateSectionName(overrideSection.getSectionName());
						if ((isIntroducedSection) && (parentSectionNames.contains(introducedSectionName))) {
							// Must override to include child introduced section
							throw designer.addIssue("Section '" + introducedSectionName
									+ "' already exists by inheritance and not flagged for overriding (with '"
									+ OVERRIDE_SECTION_PREFIX + "' prefix)");

						} else {
							// Include the override/introduced section
							inheritanceSections.add(overrideSection);
						}

						// Always introducing after the first (override) section
						isIntroducedSection = true;
					}
				}
			}

			// Provide issues for any child sections not overriding
			for (String notOverrideSectionName : overrideSections.keySet()) {
				throw designer.addIssue(
						"No inherited section exists for overriding by section '" + notOverrideSectionName + "'");
			}

			// Obtain the sections from child inheritance
			sections = inheritanceSections.toArray(new ParsedTemplateSection[inheritanceSections.size()]);
		}

		// Reconstruct the resulting inherited template content for use
		StringBuilder reconstructTemplateBuilder = new StringBuilder();
		boolean isFirstSection = true;
		for (ParsedTemplateSection section : sections) {

			// Obtain the template section name
			String sectionName = getParsedTemplateSectionName(section.getSectionName());

			// Add the section tag (only if not first default section)
			if (!((isFirstSection) && (WebTemplateParser.DEFAULT_FIRST_SECTION_NAME.equals(sectionName)))) {
				// Include section as not first default section
				reconstructTemplateBuilder.append("<!-- {" + sectionName + "} -->");
			}
			isFirstSection = false; // no longer first

			// Add the section content
			reconstructTemplateBuilder.append(section.getRawSectionContent());
		}
		String templateContent = reconstructTemplateBuilder.toString();

		// Keep track of tasks that do not render template on their completion
		Set<String> nonRenderTemplateTaskKeys = new HashSet<String>();

		// Extend the template content as necessary
		for (WebTemplateExtensionBuilderImpl extension : this.extensions) {

			// Run the extension of template
			WebTemplateExtensionContext extensionContext = new WebTemplateSectionExtensionContextImpl(templateContent,
					extension.propertyList, nonRenderTemplateTaskKeys);
			extension.webTemplateExtension.extendWebTemplate(extensionContext);

			// Override template details
			templateContent = extensionContext.getTemplateContent();
		}

		// Obtain the HTTP template
		ParsedTemplate template = WebTemplateParser.parse(new StringReader(templateContent));

		// Create the necessary dependency objects
		SectionObject connectionObject = this.getOrCreateObject(null, ServerHttpConnection.class.getName());

		// Create the I/O escalation
		SectionOutput ioEscalation = this.getOrCreateOutput(IOException.class.getName(), IOException.class.getName(),
				true);

		// Obtain configuration details for template
		boolean isTemplateSecure = Boolean
				.valueOf(context.getProperty(PROPERTY_TEMPLATE_SECURE, String.valueOf(false)));

		// Obtain the template content-type
		String templateContentType = context.getProperty(PROPERTY_CONTENT_TYPE, null);

		// Obtain the template char set
		Charset templateCharset;
		String charsetName = context.getProperty(PROPERTY_CHARSET, null);
		if (!CompileUtil.isBlank(charsetName)) {
			// Use the specified char set
			templateCharset = Charset.forName(charsetName);
		} else {
			// Use default char set
			templateCharset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		}

		// Obtain the ending path parameter terminating character
		char linkSeparatorCharacter = context
				.getProperty(PROPERTY_LINK_SEPARATOR, String.valueOf(DEFAULT_LINK_SEPARATOR)).charAt(0);

		// Load the initial function
		WebTemplateInitialFunction initialFunctionFactory = new WebTemplateInitialFunction(isTemplateSecure,
				templateContentType, templateCharset, this.inputPath, linkSeparatorCharacter);
		SectionFunctionNamespace initialNamespace = designer.addSectionFunctionNamespace("INITIAL",
				new WebTemplateInitialManagedFunctionSource(initialFunctionFactory));
		SectionFunction initialFunction = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		designer.link(initialFunction.getFunctionObject("SERVER_HTTP_CONNECTION"), connectionObject);
		designer.link(initialFunction.getFunctionEscalation(IOException.class.getName()), ioEscalation, false);

		// Create and link rendering input to initial function
		SectionInput sectionInput = designer.addSectionInput(RENDER_TEMPLATE_INPUT_NAME, null);
		designer.link(sectionInput, initialFunction);

		// Must provide logic class if path parameters
		boolean isPathParameters = Boolean
				.parseBoolean(context.getProperty(PROPERTY_IS_PATH_PARAMETERS, String.valueOf(false)));
		if (isPathParameters && (!isLogicClass)) {
			throw designer.addIssue("Must provide logic class, as template has path parameters");
		}

		// Create and link redirect of template
		SectionOutput redirectOutput = designer.addSectionOutput(REDIRECT_TEMPLATE_OUTPUT_NAME, null, false);
		FunctionFlow templateRedirectFlow = initialFunction.getFunctionFlow(Flows.REDIRECT.name());
		String redirectValuesFunctionName = context.getProperty(PROPERTY_REDIRECT_VALUES_FUNCTION, null);
		SectionFunction redirectValuesFunction = null;
		Class<?> redirectValuesType = null;
		if (redirectValuesFunctionName != null) {
			redirectValuesFunction = this.getFunctionByName(redirectValuesFunctionName);
			if (redirectValuesFunction == null) {
				// Indicate issues, as configured function is not available
				throw designer.addIssue(
						"No method by name '" + redirectValuesFunctionName + "' on logic class " + sectionClassName);
			} else {
				// Obtain the redirect values type
				redirectValuesType = this.getFunctionTypeByName(redirectValuesFunctionName).getReturnType();
			}
		}
		if (redirectValuesFunction == null) {
			// Ensure indicating issue if require values
			if (isPathParameters) {
				throw designer.addIssue(WebTemplate.class.getSimpleName()
						+ " has path parameters but no redirect values function configured");
			}

			// No redirect function so link directly to redirect
			designer.link(templateRedirectFlow, redirectOutput, false);

		} else {
			// Redirect values function, so link to it then redirect
			designer.link(templateRedirectFlow, redirectValuesFunction, false);
			designer.link(redirectValuesFunction, redirectOutput);
		}
		redirectOutput.addAnnotation(new WebTemplateRedirectAnnotation(redirectValuesType));

		// Load the HTTP template
		final String TEMPLATE_NAMESPACE_NANE = "TEMPLATE";
		SectionFunctionNamespace templateNamespace = designer.addSectionFunctionNamespace(TEMPLATE_NAMESPACE_NANE,
				new WebTemplateManagedFunctionSource(isTemplateSecure, template, templateCharset,
						linkSeparatorCharacter));
		templateNamespace.addProperty(PROPERTY_TEMPLATE_CONTENT, templateContent);

		// Copy the template configuration
		PropertiesUtil.copyProperties(context, templateNamespace, PROPERTY_TEMPLATE_SECURE, PROPERTY_CHARSET);
		PropertiesUtil.copyPrefixedProperties(context, PROPERTY_LINK_SECURE_PREFIX, templateNamespace);

		// Create the template functions and ensure registered for logic flows
		Map<String, SectionFunction> templateFunctions = new HashMap<>();
		for (ParsedTemplateSection templateSection : template.getSections()) {

			// Obtain the template function name
			String templateFunctionName = templateSection.getSectionName();

			// Add the template function
			SectionFunction templateFunction = templateNamespace.addSectionFunction(templateFunctionName,
					templateFunctionName);

			// Register the template function
			templateFunctions.put(templateFunctionName, templateFunction);
		}

		// Load the HTTP template functions
		Map<String, SectionFunction> contentFunctionsByName = new HashMap<>();
		SectionFunction previousTemplateFunction = initialFunction;
		for (ParsedTemplateSection templateSection : template.getSections()) {

			// Obtain the template function
			String templateFunctionName = templateSection.getSectionName();
			SectionFunction templateFunction = templateFunctions.get(templateFunctionName);

			// Link the dependencies (later will determine if bean dependency)
			designer.link(templateFunction.getFunctionObject("SERVER_HTTP_CONNECTION"), connectionObject);

			// Link the I/O escalation
			designer.link(templateFunction.getFunctionEscalation(IOException.class.getName()), ioEscalation, false);

			// Keep track of task for later flow linking
			contentFunctionsByName.put(createFunctionKey(templateFunctionName), templateFunction);

			// Obtain the possible bean task method for the section
			String beanMethodName = "get" + templateFunctionName;
			String beanFunctionKey = createFunctionKey(beanMethodName);
			TemplateClassFunction beanFunction = this.sectionClassMethodFunctionsByName.get(beanFunctionKey);
			if (beanFunction == null) {
				// Attempt to find with Data suffix
				beanFunctionKey = beanFunctionKey + "DATA";
				beanFunction = this.sectionClassMethodFunctionsByName.get(beanFunctionKey);
			}

			// Bean task to not render template on completion
			nonRenderTemplateTaskKeys.add(beanFunctionKey);

			// Determine if template section requires a bean
			boolean isRequireBean = false;
			for (ParsedTemplateSectionContent content : templateSection.getContent()) {
				if ((content instanceof PropertyParsedTemplateSectionContent)
						|| (content instanceof BeanParsedTemplateSectionContent)) {
					// Section contains property/bean reference, so requires
					// bean
					isRequireBean = true;
				}
			}
			if ((isRequireBean) && (beanFunction == null)) {
				// Section method required, determine if just missing method
				if (!isLogicClass) {
					// No template logic
					throw designer.addIssue("Must provide template logic class for template " + templatePath);
				} else {
					// Have template logic, so missing method
					throw designer.addIssue("Missing method '" + beanMethodName + "' on class "
							+ this.sectionClass.getName() + " to provide bean for template " + templatePath);
				}
			}

			// Validate and include the bean function
			if (beanFunction != null) {

				// Ensure bean task does not have a @Parameter
				if (beanFunction.parameter != null) {
					throw designer.addIssue("Template bean method '" + beanMethodName + "' must not have a @"
							+ Parameter.class.getSimpleName() + " annotation");
				}

				// Ensure no next function (as must render section next)
				Method method = beanFunction.method;
				if (method.isAnnotationPresent(NextFunction.class)) {
					throw designer.addIssue("Template bean method '" + method.getName() + "' (function "
							+ beanFunctionKey + ") must not be annotated with @" + NextFunction.class.getSimpleName()
							+ " (next function is always rendering template section)");
				}

				// Obtain the return type for the template
				Class<?> returnType = beanFunction.type.getReturnType();
				if ((returnType == null) || (Void.class.equals(returnType))) {
					// Must provide return if require a bean
					if (isRequireBean) {
						throw designer.addIssue("Bean method '" + beanMethodName + "' must have return value");
					}

				} else {
					// Determine bean type and whether an array
					Class<?> beanType = returnType;
					boolean isArray = returnType.isArray();
					if (isArray) {
						beanType = returnType.getComponentType();
					}

					// Inform template of bean type
					templateNamespace.addProperty(PROPERTY_BEAN_PREFIX + templateFunctionName, beanType.getName());

					// Flag bean as parameter
					templateFunction.getFunctionObject("OBJECT").flagAsParameter();

					// Handle iterating over array of beans
					if (isArray) {
						// Provide iterator function if array
						SectionFunctionNamespace arrayIteratorNamespace = designer.addSectionFunctionNamespace(
								templateFunctionName + "ArrayIterator",
								new WebTemplateArrayIteratorManagedFunctionSource(beanType));
						SectionFunction arrayIteratorFunction = arrayIteratorNamespace.addSectionFunction(
								templateFunctionName + "ArrayIterator",
								WebTemplateArrayIteratorManagedFunctionSource.FUNCTION_NAME);
						arrayIteratorFunction
								.getFunctionObject(WebTemplateArrayIteratorManagedFunctionSource.OBJECT_NAME)
								.flagAsParameter();

						// Link iteration of array to rendering
						designer.link(
								arrayIteratorFunction
										.getFunctionFlow(WebTemplateArrayIteratorManagedFunctionSource.FLOW_NAME),
								templateFunction, false);

						// Iterator is now controller for template
						templateFunction = arrayIteratorFunction;
					}
				}
			}

			// Determine if linking from initial function
			if (previousTemplateFunction == initialFunction) {
				// Link as flow from initial function
				FunctionFlow renderFlow = initialFunction.getFunctionFlow(Flows.RENDER.name());
				if (beanFunction != null) {
					// Link with bean function then template
					designer.link(renderFlow, beanFunction.function, false);
					designer.link(beanFunction.function, templateFunction);
				} else {
					// No bean function so link to template
					designer.link(renderFlow, templateFunction, false);
				}

			} else {
				// Link as next from previous function
				if (beanFunction != null) {
					// Link with bean function then template
					designer.link(previousTemplateFunction, beanFunction.function);
					designer.link(beanFunction.function, templateFunction);
				} else {
					// No bean function so link to template
					designer.link(previousTemplateFunction, templateFunction);
				}
			}

			// Template function is always previous function
			previousTemplateFunction = templateFunction;
		}

		// Link flows to template content functions
		for (TemplateFlowLink flowLink : this.flowLinks) {

			// Obtain the function flow and its name
			FunctionFlow functionFlow = flowLink.functionFlow;
			String flowName = functionFlow.getFunctionFlowName();

			// Determine if linking to content function
			SectionFunction contentFunction = contentFunctionsByName.get(createFunctionKey(flowName));
			if (contentFunction != null) {
				// Link to content function
				designer.link(functionFlow, contentFunction, false);

			} else {
				// Not linked to content function, so use default behaviour
				super.linkFunctionFlow(flowLink.functionFlow, flowLink.functionType, flowLink.flowInterfaceType,
						flowLink.flowMethod, flowLink.flowArgumentType);
			}
		}

		// Determine if any unknown configured links
		NEXT_PROPERTY: for (String propertyName : context.getPropertyNames()) {
			if (propertyName.startsWith(PROPERTY_LINK_SECURE_PREFIX)) {

				// Obtain the link name
				String configuredLinkName = propertyName.substring(PROPERTY_LINK_SECURE_PREFIX.length());

				// Ignore if known link
				if (knownLinkNames.contains(configuredLinkName)) {
					continue NEXT_PROPERTY;
				}

				// Link not exist so provide issue as invalid configuration
				throw designer.addIssue("Link '" + configuredLinkName + "' does not exist on template " + templatePath);
			}
		}

		// Register the #{link} URL continuation tasks
		ParsedLink[] links = getParsedTemplateLinkNames(template);
		for (ParsedLink link : links) {

			// Obtain the link input
			SectionInput linkInput = this.getOrCreateInput(link.linkName, null);

			// Determine if link is to be secure
			boolean isLinkSecure = isLinkSecure(link.linkName, isTemplateSecure, context);

			// Obtain the methods for the link
			String[] linkMethods = link.httpMethodNames.toArray(new String[link.httpMethodNames.size()]);
			if (linkMethods.length == 0) {
				// Use default link methods
				linkMethods = new String[] { HttpMethod.GET.getName(), HttpMethod.POST.getName() };
			}

			// Add the link annotation
			linkInput.addAnnotation(new WebTemplateLinkAnnotation(isLinkSecure, link.linkName, linkMethods));

			// Determine if linked to a function
			SectionFunction function = this.getFunctionByName(link.linkName);
			if (function == null) {
				// No function, so link to output
				SectionOutput linkOutput = this.getOrCreateOutput(link.linkName, null, false);
				this.getDesigner().link(linkInput, linkOutput);
			}
		}

		// Link bean functions to re-render template by default
		List<String> sectionClassMethodTaskNames = new ArrayList<String>(
				this.sectionClassMethodFunctionsByName.keySet());
		Collections.sort(sectionClassMethodTaskNames);
		for (String beanTaskKey : sectionClassMethodTaskNames) {

			// Determine if render template on completion
			if (!(nonRenderTemplateTaskKeys.contains(beanTaskKey))) {

				// Potentially rendering so obtain the class method
				TemplateClassFunction methodFunction = this.sectionClassMethodFunctionsByName.get(beanTaskKey);
				Method method = methodFunction.method;

				// Determine if the redirect values function
				if ((redirectValuesFunctionName != null) && (redirectValuesFunctionName.equals(method.getName()))) {
					continue; // not render (as redirect)
				}

				// Determine if not render template after
				if (method.isAnnotationPresent(NotRenderTemplateAfter.class)) {
					continue; // not render
				}

				// Determine if NextFunction, so not render template after
				if (method.isAnnotationPresent(NextFunction.class)) {
					continue; // not render
				}

				// Next task not linked, so link to render template
				designer.link(methodFunction.function, initialFunction);
			}
		}

		// Link last template task to output
		// SectionOutput output =
		// this.getOrCreateOutput(ON_COMPLETION_OUTPUT_NAME, null, false);
		// designer.link(previousTemplateFunction, output);
	}

	/**
	 * {@link SectionFunction} for the template class.
	 */
	private static class TemplateClassFunction {

		/**
		 * {@link SectionFunction}.
		 */
		private final SectionFunction function;

		/**
		 * {@link ManagedFunctionType}.
		 */
		private final ManagedFunctionType<?, ?> type;

		/**
		 * {@link Method} for the {@link SectionFunction}.
		 */
		private final Method method;

		/**
		 * Type of parameter for {@link SectionFunction}. <code>null</code> indicates no
		 * parameter.
		 */
		private final Class<?> parameter;

		/**
		 * Initiate.
		 * 
		 * @param function
		 *            {@link SectionFunction}.
		 * @param type
		 *            {@link ManagedFunctionType}.
		 * @param method
		 *            {@link Method} for the {@link SectionFunction}.
		 * @param parameter
		 *            Type of parameter for {@link SectionFunction}. <code>null</code>
		 *            indicates no parameter.
		 */
		private TemplateClassFunction(SectionFunction function, ManagedFunctionType<?, ?> type, Method method,
				Class<?> parameter) {
			this.function = function;
			this.type = type;
			this.method = method;
			this.parameter = parameter;
		}
	}

	/**
	 * Parsed link.
	 */
	private static class ParsedLink {

		/**
		 * Link name.
		 */
		private final String linkName;

		/**
		 * {@link HttpMethod} names.
		 */
		private final List<String> httpMethodNames = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param linkName
		 *            Link name.
		 */
		private ParsedLink(String linkName) {
			this.linkName = linkName;
		}
	}

	/**
	 * Template {@link FunctionFlow} instances to be linked.
	 */
	private static class TemplateFlowLink {

		/**
		 * {@link FunctionFlow} to be linked.
		 */
		private final FunctionFlow functionFlow;

		/**
		 * {@link ManagedFunctionType} of the {@link ManagedFunction} for the
		 * {@link SectionFlow}.
		 */
		private final ManagedFunctionType<?, ?> functionType;

		/**
		 * Flow interface type.
		 */
		private final Class<?> flowInterfaceType;

		/**
		 * Flow interface method.
		 */
		private final Method flowMethod;

		/**
		 * Flow interface method argument type.
		 */
		private final Class<?> flowArgumentType;

		/**
		 * Initiate.
		 * 
		 * @param functionFlow
		 *            {@link FunctionFlow} to be linked.
		 * @param functionType
		 *            {@link ManagedFunctionType} of the {@link ManagedFunction} for the
		 *            {@link FunctionFlow}.
		 * @param flowInterfaceType
		 *            Flow interface type.
		 * @param flowMethod
		 *            Flow interface method.
		 * @param flowArgumentType
		 *            Flow interface method argument type.
		 */
		private TemplateFlowLink(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
				Class<?> flowInterfaceType, Method flowMethod, Class<?> flowArgumentType) {
			this.functionFlow = functionFlow;
			this.functionType = functionType;
			this.flowInterfaceType = flowInterfaceType;
			this.flowMethod = flowMethod;
			this.flowArgumentType = flowArgumentType;
		}
	}

	/**
	 * {@link WebTemplateExtensionContext} implementation.
	 */
	private class WebTemplateSectionExtensionContextImpl extends SourcePropertiesImpl
			implements WebTemplateExtensionContext {

		/**
		 * Raw {@link ParsedTemplate} content.
		 */
		private String templateContent;

		/**
		 * {@link Set} to be populated with keys to {@link ManagedFunction} instances
		 * that are not to have the template rendered on their completion.
		 */
		private final Set<String> nonRenderTemplateTaskKeys;

		/**
		 * Initiate.
		 * 
		 * @param templateContent
		 *            Raw {@link ParsedTemplate} content.
		 * @param extensionProperties
		 *            {@link PropertyList} to configure the
		 *            {@link WebTemplateExtension}.
		 * @param nonRenderTemplateTaskKeys
		 *            {@link Set} to be populated with keys to {@link ManagedFunction}
		 *            instances that are not to have the template rendered on their
		 *            completion.
		 */
		private WebTemplateSectionExtensionContextImpl(String templateContent, PropertyList extensionProperties,
				Set<String> nonRenderTemplateTaskKeys) {
			super(new PropertyListSourceProperties(extensionProperties));
			this.templateContent = templateContent;
			this.nonRenderTemplateTaskKeys = nonRenderTemplateTaskKeys;
		}

		/*
		 * ============== WebTemplateSectionExtensionContext ================
		 */

		@Override
		public String getTemplateContent() {
			return this.templateContent;
		}

		@Override
		public void setTemplateContent(String templateContent) {
			this.templateContent = templateContent;
		}

		@Override
		public Class<?> getLogicClass() {
			return WebTemplateSectionSource.this.sectionClass;
		}

		@Override
		public void flagAsNonRenderTemplateMethod(String templateClassMethodName) {
			this.nonRenderTemplateTaskKeys.add(createFunctionKey(templateClassMethodName));
		}

		@Override
		public SectionSourceContext getSectionSourceContext() {
			return WebTemplateSectionSource.this.getContext();
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return WebTemplateSectionSource.this.getDesigner();
		}

		@Override
		public SectionManagedObject getTemplateLogicObject() {
			return WebTemplateSectionSource.this.sectionClassManagedObject;
		}

		@Override
		public SectionFunction getFunction(String functionName) {
			return WebTemplateSectionSource.this.getFunctionByName(functionName);
		}

		@Override
		public SectionObject getOrCreateSectionObject(String typeName) {
			return WebTemplateSectionSource.this.getOrCreateObject(null, typeName);
		}

		@Override
		public SectionOutput getOrCreateSectionOutput(String name, String argumentType, boolean isEscalationOnly) {
			return WebTemplateSectionSource.this.getOrCreateOutput(name, argumentType, isEscalationOnly);
		}
	}

	/**
	 * {@link ManagedFunctionSource} to provide the
	 * {@link WebTemplateInitialFunction}.
	 */
	static class WebTemplateInitialManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link WebTemplateInitialFunction}.
		 */
		public static final String FUNCTION_NAME = "FUNCTION";

		/**
		 * {@link WebTemplateInitialFunction}.
		 */
		private final WebTemplateInitialFunction function;

		/**
		 * Instantiate.
		 * 
		 * @param function
		 *            {@link WebTemplateInitialFunction}.
		 */
		WebTemplateInitialManagedFunctionSource(WebTemplateInitialFunction function) {
			this.function = function;
		}

		/*
		 * ================== ManagedFunctionSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Configure the function
			ManagedFunctionTypeBuilder<WebTemplateInitialDependencies, Flows> function = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, this.function, WebTemplateInitialDependencies.class,
							Flows.class);
			function.addObject(ServerHttpConnection.class)
					.setKey(WebTemplateInitialDependencies.SERVER_HTTP_CONNECTION);
			function.addFlow().setKey(Flows.REDIRECT);
			function.addFlow().setKey(Flows.RENDER);
			function.addEscalation(IOException.class);
		}
	}

	/**
	 * Section {@link WebTemplateWriter} struct.
	 */
	public static class SectionWriterStruct {

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
	 * {@link ManagedFunctionSource} for the HTTP template.
	 * 
	 * @author Daniel Sagenschneider
	 */
	public static class WebTemplateManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Indicates if the {@link WebTemplate} is secure.
		 */
		private final boolean isSecure;

		/**
		 * {@link ParsedTemplate}.
		 */
		private final ParsedTemplate template;

		/**
		 * Default {@link Charset} to render the {@link WebTemplate}.
		 */
		private final Charset charset;

		/**
		 * Link separator {@link Character}.
		 */
		private char linkSeparatorCharacter;

		/**
		 * Instantiate.
		 * 
		 * @param isSecure
		 *            Indicates if the {@link WebTemplate} is secure.
		 * @param template
		 *            {@link ParsedTemplate}.
		 * @param charset
		 *            Default {@link Charset} to render the {@link WebTemplate}.
		 * @param linkSeparatorCharacter
		 *            Link separator {@link Character}.
		 */
		WebTemplateManagedFunctionSource(boolean isSecure, ParsedTemplate template, Charset charset,
				char linkSeparatorCharacter) {
			this.isSecure = isSecure;
			this.template = template;
			this.charset = charset;
			this.linkSeparatorCharacter = linkSeparatorCharacter;
		}

		/**
		 * Obtains the {@link SectionWriterStruct}.
		 * 
		 * @param contents
		 *            {@link ParsedTemplateSectionContent} instances.
		 * @param beanClass
		 *            Bean {@link Class}.
		 * @param sectionAndFunctionName
		 *            Section and function name.
		 * @param linkFunctionNames
		 *            List function names.
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
		private SectionWriterStruct createWebTemplateWriters(ParsedTemplateSectionContent[] contents,
				Class<?> beanClass, String sectionAndFunctionName, Set<String> linkFunctionNames, Charset charset,
				boolean isTemplateSecure, ManagedFunctionSourceContext context) throws Exception {

			// Obtain the value retriever
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Function<Class<?>, ValueRetriever<Object>> createValueRetriever = (beanClazz) -> {
				ValueRetrieverSource source = new ValueRetrieverSource(true);
				return (ValueRetriever) source.sourceValueRetriever(beanClazz);
			};

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
						beanClass = this.getBeanClass(sectionAndFunctionName, true, context);
					}

					// Ensure have the value loader for the bean
					if (valueRetriever == null) {
						valueRetriever = createValueRetriever.apply(beanClass);
					}

					// Obtain the bean method
					String beanPropertyName = beanContent.getPropertyName();
					Class<?> beanType = valueRetriever.getValueType(beanPropertyName);
					if (beanType == null) {
						throw new Exception("Bean '" + beanPropertyName + "' can not be sourced from bean type "
								+ beanClass.getName());
					}

					// Determine if an array of beans
					boolean isArray = false;
					if (beanType.isArray()) {
						isArray = true;
						beanType = beanType.getComponentType();
					}

					// Obtain the writers for the bean
					SectionWriterStruct beanStruct = this.createWebTemplateWriters(beanContent.getContent(), beanType,
							null, linkFunctionNames, charset, isTemplateSecure, context);

					// Add the content writer
					contentWriterList
							.add(new BeanWebTemplateWriter(beanContent, valueRetriever, isArray, beanStruct.writers));

				} else if (content instanceof PropertyParsedTemplateSectionContent) {
					// Add the property template writer
					PropertyParsedTemplateSectionContent propertyContent = (PropertyParsedTemplateSectionContent) content;

					// Ensure have bean class
					if (beanClass == null) {
						beanClass = this.getBeanClass(sectionAndFunctionName, true, context);
					}

					// Ensure have the value loader for the bean
					if (valueRetriever == null) {
						valueRetriever = createValueRetriever.apply(beanClass);
					}

					// Add the content writer
					contentWriterList.add(new PropertyWebTemplateWriter(propertyContent, valueRetriever, beanClass));

				} else if (content instanceof LinkParsedTemplateSectionContent) {
					// Add the link template writer
					LinkParsedTemplateSectionContent linkContent = (LinkParsedTemplateSectionContent) content;

					// Obtain the link name
					String linkName = linkContent.getName();
					if (linkName.contains(":")) {
						linkName = linkName.split(":")[1];
					}

					// Determine if the link is to be secure
					boolean isLinkSecure = isLinkSecure(linkName, isTemplateSecure, context);

					// Add the content writer
					contentWriterList
							.add(new LinkWebTemplateWriter(linkName, isLinkSecure, this.linkSeparatorCharacter));

					// Track the link tasks
					linkFunctionNames.add(linkName);

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
		 * @param sectionAndFunctionName
		 *            Section and function name.
		 * @param context
		 *            {@link SourceContext}.
		 * @return Bean {@link Class}.
		 */
		private Class<?> getBeanClass(String sectionAndFunctionName, boolean isRequired, SourceContext context) {

			// Obtain the bean class name
			String beanClassPropertyName = PROPERTY_BEAN_PREFIX + sectionAndFunctionName;
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

		/*
		 * =================== AbstractWorkSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Define the functions
			for (ParsedTemplateSection section : this.template.getSections()) {

				// Obtain the section and function name
				String sectionAndFunctionName = section.getSectionName();

				// Set of link function names
				Set<String> linkFunctionNames = new HashSet<String>();

				// Optional bean class
				Class<?> beanClass = this.getBeanClass(sectionAndFunctionName, false, context);

				// Create the content writers for the section
				SectionWriterStruct writerStruct = this.createWebTemplateWriters(section.getContent(), beanClass,
						sectionAndFunctionName, linkFunctionNames, this.charset, this.isSecure, context);

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

	/**
	 * Iterates over the array objects sending them to the {@link ParsedTemplate}
	 * for rendering.
	 * 
	 * @author Daniel Sagenschneider
	 */
	static class WebTemplateArrayIteratorManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "iterate";

		/**
		 * Name of the {@link FunctionObject} providing array.
		 */
		private static final String OBJECT_NAME = DependencyKeys.ARRAY.name();

		/**
		 * Name of the {@link FunctionFlow} for rendering.
		 */
		private static final String FLOW_NAME = FlowKeys.RENDER_ELEMENT.name();

		/**
		 * Component type of the array.
		 */
		private final Class<?> componentType;

		/**
		 * Instantiate.
		 * 
		 * @param componentType
		 *            Component type of the array.
		 */
		WebTemplateArrayIteratorManagedFunctionSource(Class<?> componentType) {
			this.componentType = componentType;
		}

		/*
		 * ====================== ManagedFunctionSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the array type (from component type)
			Class<?> arrayType = Array.newInstance(this.componentType, 0).getClass();

			// Create the function
			WebTemplateArrayIteratorFunction function = new WebTemplateArrayIteratorFunction();

			// Specify the function
			ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> functionBuilder = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, function, DependencyKeys.class, FlowKeys.class);
			functionBuilder.addObject(arrayType).setKey(DependencyKeys.ARRAY);
			ManagedFunctionFlowTypeBuilder<FlowKeys> flow = functionBuilder.addFlow();
			flow.setKey(FlowKeys.RENDER_ELEMENT);
			flow.setArgumentType(this.componentType);
		}

	}

	/*
	 * =================== ClassSectionSource ==========================
	 */

	@Override
	protected String getSectionClassName() {
		return this.sectionClass.getName();
	}

	@Override
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {
		return this.sectionClass;
	}

	@Override
	protected SectionManagedObject createClassManagedObject(String objectName, Class<?> sectionClass) {

		// Determine if already loaded the Section Managed Object
		if (this.sectionClassManagedObject != null) {
			return this.sectionClassManagedObject; // instance
		}

		// Determine if stateful
		boolean isStateful = isHttpSessionStateful(sectionClass);

		// Default behaviour if not stateful
		if (!isStateful) {
			// Defer to default behaviour
			this.sectionClassManagedObject = super.createClassManagedObject(objectName, sectionClass);

		} else {
			// As stateful, the class must be serialisable
			if (!(Serializable.class.isAssignableFrom(sectionClass))) {
				throw this.getDesigner()
						.addIssue("Template logic class " + sectionClass.getName() + " is annotated with "
								+ HttpSessionStateful.class.getSimpleName() + " but is not "
								+ Serializable.class.getSimpleName());
			}

			// Create the managed object for the stateful template logic
			SectionManagedObjectSource managedObjectSource = this.getDesigner()
					.addSectionManagedObjectSource(objectName, HttpSessionObjectManagedObjectSource.class.getName());
			managedObjectSource.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
					sectionClass.getName());

			// Create the managed object
			this.sectionClassManagedObject = managedObjectSource.addSectionManagedObject(objectName,
					ManagedObjectScope.PROCESS);
		}

		// Return the managed object
		return this.sectionClassManagedObject;
	}

	@Override
	protected DependencyMetaData[] extractClassManagedObjectDependencies(String objectName, Class<?> sectionClass)
			throws Exception {

		// Extract the dependency meta-data for default behaviour
		DependencyMetaData[] metaData = super.extractClassManagedObjectDependencies(objectName, sectionClass);

		// Determine if stateful
		boolean isStateful = isHttpSessionStateful(sectionClass);

		// If not stateful, return meta-data for default behaviour
		if (!isStateful) {
			return metaData;
		}

		// As stateful, must not have any dependencies into object
		if (metaData.length > 0) {
			throw this.getDesigner()
					.addIssue("Template logic class " + sectionClass.getName() + " is annotated with "
							+ HttpSessionStateful.class.getSimpleName()
							+ " and therefore can not have dependencies injected into the object (only its methods)");
		}

		// Return the dependency meta-data for stateful template logic
		return new DependencyMetaData[] { new StatefulDependencyMetaData() };
	}

	@Override
	protected void enrichFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType, Method method,
			Class<?> parameterType) {

		// Do not include if no logic class
		if (NoLogicClass.class.equals(this.sectionClass)) {
			return;
		}

		// Keep track of the functions to allow linking by case-insensitive
		// names
		String functionKey = createFunctionKey(function.getSectionFunctionName());
		this.sectionClassMethodFunctionsByName.put(functionKey,
				new TemplateClassFunction(function, functionType, method, parameterType));

		// Enrich the function
		super.enrichFunction(function, functionType, method, parameterType);
	}

	@Override
	protected void linkFunctionFlow(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
			Class<?> flowInterfaceType, Method flowMethod, Class<?> flowArgumentType) {
		// At this stage, the template content functions are not available.
		// Therefore just keep track of flows for later linking.
		this.flowLinks
				.add(new TemplateFlowLink(functionFlow, functionType, flowInterfaceType, flowMethod, flowArgumentType));
	}

	/**
	 * {@link WebTemplateExtensionBuilder} implementation.
	 */
	private static class WebTemplateExtensionBuilderImpl implements WebTemplateExtensionBuilder {

		/**
		 * {@link WebTemplateExtension}.
		 */
		private final WebTemplateExtension webTemplateExtension;

		/**
		 * {@link PropertyList} to configure the {@link WebTemplateExtension}.
		 */
		private final PropertyList propertyList;

		/**
		 * Instantiate.
		 * 
		 * @param webTemplateExtension
		 *            {@link WebTemplateExtension}.
		 * @param propertyList
		 *            {@link PropertyList} to configure the
		 *            {@link WebTemplateExtension}.
		 */
		private WebTemplateExtensionBuilderImpl(WebTemplateExtension webTemplateExtension, PropertyList propertyList) {
			this.webTemplateExtension = webTemplateExtension;
			this.propertyList = propertyList;
		}

		/*
		 * =============== WebTemplateExtensionBuilder ==================
		 */

		@Override
		public void addProperty(String name, String value) {
			this.propertyList.addProperty(name).setValue(value);
		}
	}

}