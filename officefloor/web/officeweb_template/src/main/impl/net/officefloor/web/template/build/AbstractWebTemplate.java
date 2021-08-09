/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.template.build;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.build.HttpSecurableBuilder;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * Abstract {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWebTemplate implements WebTemplate {

	/**
	 * {@link WebTemplateSectionSource}.
	 */
	protected final WebTemplateSectionSource webTemplateSectionSource;

	/**
	 * Indicates if the {@link WebTemplate} is secure.
	 */
	protected final boolean isSecure;

	/**
	 * Application path for the {@link WebTemplate}.
	 */
	protected final String applicationPath;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * {@link SourceIssues}.
	 */
	private final SourceIssues sourceIssues;

	/**
	 * Name of the logic {@link Class} for the {@link WebTemplate}.
	 */
	protected String logicClassName = null;

	/**
	 * Name of {@link Method} on the logic {@link Class} to return the values for
	 * path parameters in redirecting to this {@link WebTemplate}.
	 */
	protected String redirectValuesFunctionName = null;

	/**
	 * Secure links.
	 */
	protected Map<String, Boolean> secureLinks = new HashMap<>();

	/**
	 * Link separator character.
	 */
	protected char linkSeparatorCharacter = '+';

	/**
	 * <code>Content-Type</code> for the {@link WebTemplate}.
	 */
	protected String contentType = null;

	/**
	 * Name of the {@link Charset}.
	 */
	protected String charsetName = null;

	/**
	 * Render {@link HttpMethod} names.
	 */
	protected final List<String> renderHttpMethodNames = new LinkedList<>();

	/**
	 * Super {@link WebTemplate}.
	 */
	protected AbstractWebTemplate superTemplate = null;

	/**
	 * Instantiate.
	 * 
	 * @param webTemplateSectionSource
	 *            {@link WebTemplateSectionSource} instance to use.
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path for the {@link WebTemplate}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param sourceIssues
	 *            {@link SourceIssues}.
	 */
	protected AbstractWebTemplate(WebTemplateSectionSource webTemplateSectionSource, boolean isSecure,
			String applicationPath, PropertyList properties, SourceIssues sourceIssues) {
		this.webTemplateSectionSource = webTemplateSectionSource;
		this.isSecure = isSecure;
		this.applicationPath = applicationPath;
		this.properties = properties;
		this.sourceIssues = sourceIssues;
	}

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	protected abstract PropertyList createPropertyList();

	/**
	 * Obtains the application path.
	 * 
	 * @return Application path.
	 */
	public String getApplicationPath() {
		return this.applicationPath;
	}

	/**
	 * Obtains the {@link WebTemplateSectionSource}.
	 * 
	 * @return {@link WebTemplateSectionSource}.
	 */
	public WebTemplateSectionSource getWebTemplateSectionSource() {
		return this.webTemplateSectionSource;
	}

	/**
	 * Loads the properties.
	 * 
	 * @param isPathParameters
	 *            Indicates if path parameters.
	 * @return {@link PropertyList}.
	 */
	public PropertyList loadProperties(boolean isPathParameters) {

		// Configure properties for the template
		if (this.logicClassName != null) {
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME)
					.setValue(this.logicClassName);
		}
		if (this.redirectValuesFunctionName != null) {
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_REDIRECT_VALUES_FUNCTION)
					.setValue(this.redirectValuesFunctionName);
		}
		this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_LINK_SEPARATOR)
				.setValue(String.valueOf(this.linkSeparatorCharacter));
		if (this.contentType != null) {
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CONTENT_TYPE).setValue(this.contentType);
		}
		if (this.charsetName != null) {
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_CHARSET).setValue(this.charsetName);
		}
		this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_SECURE)
				.setValue(String.valueOf(this.isSecure));
		for (String linkName : this.secureLinks.keySet()) {
			Boolean isLinkSecure = this.secureLinks.get(linkName);
			this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_LINK_SECURE_PREFIX + linkName)
					.setValue(String.valueOf(isLinkSecure));
		}

		// Configure inheritance properties
		Deque<AbstractWebTemplate> inheritanceHeirarchy = new LinkedList<>();
		AbstractWebTemplate parent = this.superTemplate;
		StringBuilder cycle = new StringBuilder();
		cycle.append(this.applicationPath + " ::");
		while (parent != null) {

			// Determine if inheritance cycle
			if (inheritanceHeirarchy.contains(parent)) {
				throw this.sourceIssues.addIssue(
						WebTemplate.class.getSimpleName() + " inheritance cycle " + cycle.toString() + " ...");
			}

			// Include parent on report for cycle
			cycle.append(" " + parent.applicationPath + " ::");

			// Include in inheritance hierarchy
			inheritanceHeirarchy.push(parent);
			parent = parent.superTemplate;
		}
		this.properties.getOrAddProperty(WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES_COUNT)
				.setValue(String.valueOf(inheritanceHeirarchy.size()));
		int inheritanceIndex = 0;
		while (inheritanceHeirarchy.size() > 0) {
			parent = inheritanceHeirarchy.pop();

			// Attempt to load content
			Property parentContent = parent.properties.getProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT);
			if (parentContent != null) {
				this.properties.getOrAddProperty(
						WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT + "." + String.valueOf(inheritanceIndex))
						.setValue(parentContent.getValue());
			} else {
				Property parentLocation = parent.properties
						.getProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION);
				this.properties.getOrAddProperty(
						WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION + "." + String.valueOf(inheritanceIndex))
						.setValue(parentLocation.getValue());
			}

			// Next parent
			inheritanceIndex++;
		}

		// Ensure appropriately configured
		if (isPathParameters) {

			// Must have logic class
			if (this.logicClassName == null) {
				throw this.sourceIssues.addIssue("Must provide template logic class for template "
						+ this.applicationPath + ", as has dynamic path");
			}

			// Must have redirect values function
			if (CompileUtil.isBlank(this.redirectValuesFunctionName)) {
				throw this.sourceIssues
						.addIssue("Must provide redirect values function for template /{param}, as has dynamic path");
			}
		}

		// Return the properties
		return this.properties;
	}

	/*
	 * ================== WebTemplate ============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public WebTemplate setLogicClass(String logicClassName) {
		this.logicClassName = logicClassName;
		return this;
	}

	@Override
	public WebTemplate setRedirectValuesFunction(String functionName) {
		this.redirectValuesFunctionName = functionName;
		return this;
	}

	@Override
	public WebTemplate setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public WebTemplate setCharset(String charsetName) {
		this.charsetName = charsetName;
		return this;
	}

	@Override
	public WebTemplate setLinkSeparatorCharacter(char separator) {
		this.linkSeparatorCharacter = separator;
		return this;
	}

	@Override
	public WebTemplate setLinkSecure(String linkName, boolean isSecure) {
		this.secureLinks.put(linkName, isSecure);
		return this;
	}

	@Override
	public HttpSecurableBuilder getHttpSecurer() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement Security for WebTemplate");
	}

	@Override
	public WebTemplate addRenderHttpMethod(String httpMethodName) {
		this.renderHttpMethodNames.add(httpMethodName);
		return this;
	}

	@Override
	public WebTemplate setSuperTemplate(WebTemplate superTemplate) {
		this.superTemplate = (AbstractWebTemplate) superTemplate;
		return this;
	}

	@Override
	public WebTemplateExtensionBuilder addExtension(String webTemplateExtensionClassName) {
		return this.webTemplateSectionSource.addWebTemplateExtension(webTemplateExtensionClassName,
				this.createPropertyList());
	}

}
