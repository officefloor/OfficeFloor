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
package net.officefloor.eclipse.wizard.access;

import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.wizard.sectionsource.SectionSourceInstance;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofAccessInputModel;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofAccessOutputModel;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * Instance of a {@link WoofAccessModel} and its modelling of
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AccessInstance {

	/**
	 * {@link HttpSecuritySource} class name.
	 */
	private final String httpSecuritySourceClassName;

	/**
	 * Timeout of the authentication.
	 */
	private final long authenticationTimeout;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<?, ?, ?, ?> httpSecurityType;

	/**
	 * {@link WoofAccessModel}.
	 */
	private final WoofAccessModel accessModel;

	/**
	 * Mapping of {@link HttpSecurityType} input names to
	 * {@link WoofAccessInputModel} name.
	 */
	private final Map<String, String> inputNameMapping;

	/**
	 * Mapping of {@link HttpSecurityType} output names to
	 * {@link WoofAccessOutputModel} name.
	 */
	private final Map<String, String> outputNameMapping;

	/**
	 * Initiate for public use.
	 * 
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param authenticationTimeout
	 *            Authentication timeout.
	 */
	public AccessInstance(String httpSecuritySourceClassName,
			long authenticationTimeout) {
		this.httpSecuritySourceClassName = httpSecuritySourceClassName;
		this.authenticationTimeout = authenticationTimeout;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.httpSecurityType = null;
		this.accessModel = null;
		this.inputNameMapping = null;
		this.outputNameMapping = null;
	}

	/**
	 * Initiate for public use from {@link WoofAccessModel}.
	 * 
	 * @param model
	 *            {@link WoofAccessInputModel}.
	 */
	public AccessInstance(WoofAccessModel model) {
		this.httpSecuritySourceClassName = model
				.getHttpSecuritySourceClassName();
		this.authenticationTimeout = model.getTimeout();
		this.httpSecurityType = null;
		this.accessModel = model;
		this.inputNameMapping = null;
		this.outputNameMapping = null;

		// Load the properties
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : model.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}
	}

	/**
	 * Initiate from {@link HttpSecuritySourceInstance}.
	 * 
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param authenticationTimeout
	 *            Authentication timeout.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 * @param inputNameMapping
	 *            Mapping of {@link HttpSecurityType} input name to
	 *            {@link WoofAccessInputModel} name.
	 * @param outputNameMapping
	 *            Mapping of {@link HttpSecurityType} output name to
	 *            {@link WoofAccessOutputModel} name.
	 */
	AccessInstance(String httpSecuritySourceClassName,
			long authenticationTimeout, PropertyList propertyList,
			HttpSecurityType<?, ?, ?, ?> httpSecurityType,
			Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping) {
		this.httpSecuritySourceClassName = httpSecuritySourceClassName;
		this.authenticationTimeout = authenticationTimeout;
		this.propertyList = propertyList;
		this.httpSecurityType = httpSecurityType;
		this.accessModel = null;
		this.inputNameMapping = inputNameMapping;
		this.outputNameMapping = outputNameMapping;
	}

	/**
	 * Obtains the {@link HttpSecuritySource} class name.
	 * 
	 * @return {@link HttpSecuritySource} class name.
	 */
	public String getHttpSecuritySourceClassName() {
		return this.httpSecuritySourceClassName;
	}

	/**
	 * Obtains the authentication timeout.
	 * 
	 * @return Authentication timeout.
	 */
	public long getAuthenticationTimeout() {
		return this.authenticationTimeout;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType} if obtained from
	 *         {@link SectionSourceInstance} or <code>null</code> if initiated
	 *         by <code>public</code> constructor.
	 */
	public HttpSecurityType<?, ?, ?, ?> getHttpSecurityType() {
		return this.httpSecurityType;
	}

	/**
	 * Obtains the {@link WoofAccessModel}.
	 * 
	 * @return {@link WoofAccessModel}.
	 */
	public WoofAccessModel getWoofAccessModel() {
		return this.accessModel;
	}

	/**
	 * Obtains the input name mapping.
	 * 
	 * @return Input name mapping.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputNameMapping;
	}

	/**
	 * Obtains the output name mapping.
	 * 
	 * @return Output name mapping.
	 */
	public Map<String, String> getOutputNameMapping() {
		return this.outputNameMapping;
	}

}