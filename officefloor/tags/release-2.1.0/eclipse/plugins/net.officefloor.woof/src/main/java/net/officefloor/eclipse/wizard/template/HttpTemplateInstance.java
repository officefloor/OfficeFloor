/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.compile.section.SectionType;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Instance of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInstance {

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
	 * URI.
	 */
	private final String uri;

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
	 * Initiate.
	 * 
	 * @param templatePath
	 *            Path to the {@link HttpTemplate}.
	 * @param logicClassName
	 *            Name of the logic class.
	 * @param sectionType
	 *            {@link SectionType}.
	 * @param uri
	 *            URI.
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
	public HttpTemplateInstance(String templatePath, String logicClassName,
			SectionType sectionType, String uri, String gwtEntryPointClassName,
			String[] gwtServerAsyncInterfaceNames, boolean isEnableComet,
			String cometManualPublishMethodName) {
		this.templatePath = templatePath;
		this.logicClassName = logicClassName;
		this.sectionType = sectionType;
		this.uri = uri;
		this.gwtEntryPointClassName = gwtEntryPointClassName;
		this.gwtServerAsyncInterfaceNames = gwtServerAsyncInterfaceNames;
		this.isEnableComet = isEnableComet;
		this.cometManualPublishMethodName = cometManualPublishMethodName;
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
	 * Obtains the URI.
	 * 
	 * @return URI.
	 */
	public String getUri() {
		return this.uri;
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

}