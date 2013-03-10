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
package net.officefloor.eclipse.wizard.template;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofTemplateInheritance;
import net.officefloor.model.woof.WoofTemplateLinkModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateRedirectModel;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Instance of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInstance {

	/**
	 * Name of the {@link WoofTemplateModel}.
	 */
	private final String woofTemplateName;

	/**
	 * URI.
	 */
	private final String uri;

	/**
	 * Path to the {@link HttpTemplate}.
	 */
	private final String templatePath;

	/**
	 * Name of the logic class.
	 */
	private final String logicClassName;

	/**
	 * {@link SectionType}.
	 */
	private final SectionType sectionType;

	/**
	 * Super {@link WoofTemplateModel}.
	 */
	private final WoofTemplateModel superTemplate;

	/**
	 * Names of the {@link WoofTemplateOutputModel} instances inheriting their
	 * configuration.
	 */
	private final Set<String> inheritedTemplateOutputNames;

	/**
	 * Indicates if the {@link HttpTemplate} requires a secure
	 * {@link ServerHttpConnection}.
	 */
	private final boolean isTemplateSecure;

	/**
	 * {@link HttpTemplate} links requiring specific secure configuration.
	 */
	private final Map<String, Boolean> linksSecure;

	/**
	 * HTTP methods that trigger a redirect on rendering the
	 * {@link HttpTemplate}.
	 */
	private final String[] renderRedirectHttpMethods;

	/**
	 * Indicates if allow to continue rendering. As continue rendering is
	 * typically used very seldomly (if at all) within applications this is
	 * <code>false</code> by default to avoid cluttering space in the WoOF
	 * configuration.
	 */
	private final boolean isContinueRendering;

	/**
	 * GWT EntryPoint class name.
	 */
	private final String gwtEntryPointClassName;

	/**
	 * GWT Service Async Interface names.
	 */
	private final String[] gwtServerAsyncInterfaceNames;

	/**
	 * Flag indicating if to enable Comet for the template.
	 */
	private final boolean isEnableComet;

	/**
	 * {@link Method} name on the template logic {@link Class} to handle
	 * manually publishing a Comet event. May be <code>null</code> to
	 * automatically handle.
	 */
	private final String cometManualPublishMethodName;

	/**
	 * {@link WoofTemplateOutputModel} names on the {@link WoofTemplateModel}
	 * being refactored.
	 */
	private final String[] outputNames;

	/**
	 * Mapping of {@link SectionOutputType} name to existing
	 * {@link WoofTemplateOutputModel} name.
	 */
	private final Map<String, String> ouputNameMapping;

	/**
	 * Initiate.
	 * 
	 * @param woofTemplateName
	 *            {@link WoofTemplateModel} name.
	 * @param uri
	 *            URI.
	 * @param templatePath
	 *            Path to the {@link HttpTemplate}.
	 * @param logicClassName
	 *            Name of the logic class.
	 * @param sectionType
	 *            {@link SectionType}.
	 * @param superTemplate
	 *            Super {@link WoofTemplateModel}. May be <code>null</code> if
	 *            not inheriting.
	 * @param inheritedTemplateOutputNames
	 *            Names of the {@link WoofTemplateOutputModel} instances that
	 *            are inheriting their configuration.
	 * @param isTemplateSecure
	 *            Indicates if the {@link HttpTemplate} requires a secure
	 *            {@link ServerHttpConnection}.
	 * @param linksSecure
	 *            {@link HttpTemplate} links requiring specific secure
	 *            configuration.
	 * @param renderRedirectHttpMethods
	 *            HTTP methods that trigger a redirect on rendering the
	 *            {@link HttpTemplate}.
	 * @param iscContinueRendering
	 *            Indicates if allow to continue rendering.
	 * @param gwtEntryPointClassName
	 *            GWT EntryPoint class name.
	 * @param gwtServerAsyncInterfaceNames
	 *            GWT Service Async Interface names.
	 * @param isEnableComet
	 *            Flag indicating if to enable Comet for the template.
	 * @param cometManualPublishMethodName
	 *            {@link Method} name on the template logic {@link Class} to
	 *            handle manually publishing a Comet event. May be
	 *            <code>null</code> to automatically handle.
	 */
	public HttpTemplateInstance(String woofTemplateName, String uri,
			String templatePath, String logicClassName,
			SectionType sectionType, WoofTemplateModel superTemplate,
			Set<String> inheritedTemplateOutputNames, boolean isTemplateSecure,
			Map<String, Boolean> linksSecure,
			String[] renderRedirectHttpMethods, boolean isContinueRendering,
			String gwtEntryPointClassName,
			String[] gwtServerAsyncInterfaceNames, boolean isEnableComet,
			String cometManualPublishMethodName) {
		this.woofTemplateName = woofTemplateName;
		this.uri = uri;
		this.templatePath = templatePath;
		this.logicClassName = logicClassName;
		this.sectionType = sectionType;
		this.superTemplate = superTemplate;
		this.inheritedTemplateOutputNames = inheritedTemplateOutputNames;
		this.isTemplateSecure = isTemplateSecure;
		this.linksSecure = linksSecure;
		this.renderRedirectHttpMethods = renderRedirectHttpMethods;
		this.isContinueRendering = isContinueRendering;
		this.gwtEntryPointClassName = gwtEntryPointClassName;
		this.gwtServerAsyncInterfaceNames = gwtServerAsyncInterfaceNames;
		this.isEnableComet = isEnableComet;
		this.cometManualPublishMethodName = cometManualPublishMethodName;
		this.outputNames = null;
		this.ouputNameMapping = null;
	}

	/**
	 * Initiate deriving details from {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @param changes
	 *            {@link WoofChanges}.
	 */
	public HttpTemplateInstance(WoofTemplateModel template, WoofChanges changes) {
		this.woofTemplateName = template.getWoofTemplateName();
		this.uri = template.getUri();
		this.templatePath = template.getTemplatePath();
		this.logicClassName = template.getTemplateClassName();
		this.sectionType = null;
		this.isTemplateSecure = template.getIsTemplateSecure();
		this.isContinueRendering = template.getIsContinueRendering();

		// Obtain the inheritance configuration
		WoofTemplateModel superTemplate = null;
		Set<String> inheritedTemplateOutputNames = null;
		String superTemplateName = template.getSuperTemplate();
		if (superTemplateName != null) {
			// Obtain the inheritance for the super template
			Map<String, WoofTemplateInheritance> templateInheritances = changes
					.getWoofTemplateInheritances();
			WoofTemplateInheritance inheritance = templateInheritances
					.get(superTemplateName);
			if (inheritance != null) {
				// Obtain the inheritance details
				superTemplate = inheritance.getSuperTemplate();
				inheritedTemplateOutputNames = inheritance
						.getInheritedWoofTemplateOutputNames();
			}
		}
		this.superTemplate = superTemplate;
		this.inheritedTemplateOutputNames = inheritedTemplateOutputNames;

		// Load the links
		this.linksSecure = new HashMap<String, Boolean>();
		for (WoofTemplateLinkModel link : template.getLinks()) {
			this.linksSecure.put(link.getWoofTemplateLinkName(),
					link.getIsLinkSecure());
		}

		// Load the redirects
		List<String> redirectMethods = new LinkedList<String>();
		for (WoofTemplateRedirectModel redirect : template.getRedirects()) {
			redirectMethods.add(redirect.getWoofTemplateRedirectHttpMethod());
		}
		this.renderRedirectHttpMethods = redirectMethods
				.toArray(new String[redirectMethods.size()]);

		// Obtain the extension details
		this.gwtEntryPointClassName = changes
				.getGwtEntryPointClassName(template);
		this.gwtServerAsyncInterfaceNames = changes
				.getGwtAsyncServiceInterfaceNames(template);
		this.isEnableComet = changes.isCometEnabled(template);
		this.cometManualPublishMethodName = changes
				.getCometManualPublishMethodName(template);

		// Create the listing of output names
		List<WoofTemplateOutputModel> templateOutputs = template.getOutputs();
		List<String> outputNameListing = new ArrayList<String>(
				templateOutputs.size());
		for (WoofTemplateOutputModel templateOutput : templateOutputs) {
			outputNameListing.add(templateOutput.getWoofTemplateOutputName());
		}
		this.outputNames = outputNameListing
				.toArray(new String[outputNameListing.size()]);

		// No output name mapping
		this.ouputNameMapping = null;
	}

	/**
	 * Initiate from {@link HttpTemplateWizard}.
	 * 
	 * @param woofTemplateName
	 *            {@link WoofTemplateModel} name.
	 * @param uri
	 *            URI.
	 * @param templatePath
	 *            Path to the {@link HttpTemplate}.
	 * @param logicClassName
	 *            Name of the logic class.
	 * @param sectionType
	 *            {@link SectionType}.
	 * @param superTemplate
	 *            Super {@link WoofTemplateModel}. May be <code>null</code> if
	 *            not inheriting.
	 * @param inheritedTemplateOutputNames
	 *            Names of the {@link WoofTemplateOutputModel} instances that
	 *            are inheriting their configuration.
	 * @param isTemplateSecure
	 *            Indicates if the {@link HttpTemplate} requires a secure
	 *            {@link ServerHttpConnection}.
	 * @param linksSecure
	 *            {@link HttpTemplate} links requiring specific secure
	 *            configuration.
	 * @param renderRedirectHttpMethods
	 *            HTTP methods that trigger a redirect on rendering the
	 *            {@link HttpTemplate}.
	 * @param isContinueRendering
	 *            Indicates if allow to continue rendering.
	 * @param gwtEntryPointClassName
	 *            GWT EntryPoint class name.
	 * @param gwtServerAsyncInterfaceNames
	 *            GWT Service Async Interface names.
	 * @param isEnableComet
	 *            Flag indicating if to enable Comet for the template.
	 * @param cometManualPublishMethodName
	 *            {@link Method} name on the template logic {@link Class} to
	 *            handle manually publishing a Comet event. May be
	 *            <code>null</code> to automatically handle.
	 * @param outputNameMapping
	 *            Mapping of {@link SectionOutputType} name to existing
	 *            {@link WoofTemplateOutputModel} name.
	 */
	HttpTemplateInstance(String woofTemplateName, String uri,
			String templatePath, String logicClassName,
			SectionType sectionType, WoofTemplateModel superTemplate,
			Set<String> inheritedTemplateOutputNames, boolean isTemplateSecure,
			Map<String, Boolean> linksSecure,
			String[] renderRedirectHttpMethods, boolean isContinueRendering,
			String gwtEntryPointClassName,
			String[] gwtServerAsyncInterfaceNames, boolean isEnableComet,
			String cometManualPublishMethodName,
			Map<String, String> outputNameMapping) {
		this.woofTemplateName = woofTemplateName;
		this.uri = uri;
		this.templatePath = templatePath;
		this.logicClassName = logicClassName;
		this.sectionType = sectionType;
		this.superTemplate = superTemplate;
		this.inheritedTemplateOutputNames = inheritedTemplateOutputNames;
		this.isTemplateSecure = isTemplateSecure;
		this.linksSecure = linksSecure;
		this.renderRedirectHttpMethods = renderRedirectHttpMethods;
		this.isContinueRendering = isContinueRendering;
		this.gwtEntryPointClassName = gwtEntryPointClassName;
		this.gwtServerAsyncInterfaceNames = gwtServerAsyncInterfaceNames;
		this.isEnableComet = isEnableComet;
		this.cometManualPublishMethodName = cometManualPublishMethodName;
		this.outputNames = null;
		this.ouputNameMapping = outputNameMapping;
	}

	/**
	 * Obtains the {@link WoofTemplateModel} name.
	 * 
	 * @return {@link WoofTemplateModel} name.
	 */
	public String getWoofTemplateName() {
		return this.woofTemplateName;
	}

	/**
	 * Obtains the URI.
	 * 
	 * @return URI.
	 */
	public String getUri() {
		return this.uri;
	}

	/**
	 * Obtains the path to the {@link HttpTemplate}.
	 * 
	 * @return Path to the {@link HttpTemplate}.
	 */
	public String getTemplatePath() {
		return this.templatePath;
	}

	/**
	 * Obtains the name of the logic class for the {@link HttpTemplate}.
	 * 
	 * @return Name of the logic class for the {@link HttpTemplate}.
	 */
	public String getLogicClassName() {
		return this.logicClassName;
	}

	/**
	 * Obtains the {@link SectionType}.
	 * 
	 * @return {@link SectionType}.
	 */
	public SectionType getTemplateSectionType() {
		return this.sectionType;
	}

	/**
	 * Obtains the super {@link WoofTemplateModel}.
	 * 
	 * @return Super {@link WoofTemplateModel} or <code>null</code> if not
	 *         extending a {@link WoofTemplateModel}.
	 */
	public WoofTemplateModel getSuperTemplate() {
		return this.superTemplate;
	}

	/**
	 * Obtains the names of the {@link WoofTemplateOutputModel} instances that
	 * are inheriting their configuration.
	 * 
	 * @return Inherited {@link WoofTemplateOutputModel} names.
	 */
	public Set<String> getInheritedTemplateOutputNames() {
		return this.inheritedTemplateOutputNames;
	}

	/**
	 * Obtains whether the {@link HttpTemplate} requires a secure
	 * {@link ServerHttpConnection}.
	 * 
	 * @return <code>true</code> should the {@link HttpTemplate} require a
	 *         secure {@link ServerHttpConnection}.
	 */
	public boolean isTemplateSecure() {
		return this.isTemplateSecure;
	}

	/**
	 * Obtains the mapping of secure for {@link HttpTemplate} links.
	 * 
	 * @return Mapping of secure for {@link HttpTemplate} links.
	 */
	public Map<String, Boolean> getLinksSecure() {
		return this.linksSecure;
	}

	/**
	 * Obtains the HTTP methods that will trigger a redirect on rendering the
	 * {@link HttpTemplate}.
	 * 
	 * @return HTTP methods that will trigger a redirect on rendering the
	 *         {@link HttpTemplate}.
	 */
	public String[] getRenderRedirectHttpMethods() {
		return this.renderRedirectHttpMethods;
	}

	/**
	 * Indicates if may continue rendering.
	 * 
	 * @return <code>true</code> to allow continue rendering.
	 */
	public boolean isContinueRendering() {
		return this.isContinueRendering;
	}

	/**
	 * Obtains the GWT EntryPoint class name.
	 * 
	 * @return GWT EntryPoint class name.
	 */
	public String getGwtEntryPointClassName() {
		return this.gwtEntryPointClassName;
	}

	/**
	 * Obtains the GWT Service Async Interface names.
	 * 
	 * @return GWT Service Async Interface names.
	 */
	public String[] getGwtServerAsyncInterfaceNames() {
		return this.gwtServerAsyncInterfaceNames;
	}

	/**
	 * Obtains flag indicating if to enable Comet for the template.
	 * 
	 * @return <code>true</code> to enable Comet for the template.
	 */
	public boolean isEnableComet() {
		return this.isEnableComet;
	}

	/**
	 * Obtains the {@link Method} name on the template logic {@link Class} to
	 * handle manually publishing a Comet event. May be <code>null</code> to
	 * automatically handle.
	 * 
	 * @return Manual publish {@link Method} name.
	 */
	public String getCometManualPublishMethodName() {
		return this.cometManualPublishMethodName;
	}

	/**
	 * Obtains the {@link WoofTemplateOutputModel} names on the
	 * {@link WoofTemplateModel} being refactored.
	 * 
	 * @return {@link WoofTemplateOutputModel} names on the
	 *         {@link WoofTemplateModel} being refactored. May be
	 *         <code>null</code>
	 */
	public String[] getTemplateOutputNames() {
		return this.outputNames;
	}

	/**
	 * Obtains the mapping of refactored {@link SectionOutputType} name to
	 * existing {@link WoofTemplateOutputModel} name.
	 * 
	 * @return Mapping of refactored {@link SectionOutputType} name to existing
	 *         {@link WoofTemplateOutputModel} name. May be <code>null</code>.
	 */
	public Map<String, String> getOutputNameMapping() {
		return this.ouputNameMapping;
	}

}