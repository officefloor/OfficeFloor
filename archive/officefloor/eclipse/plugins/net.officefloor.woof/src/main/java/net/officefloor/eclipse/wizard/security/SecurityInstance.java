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
package net.officefloor.eclipse.wizard.security;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofSecurityContentTypeModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;

/**
 * Instance of a {@link WoofSecurityModel} and its modelling of
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecurityInstance {

	/**
	 * Name of the {@link WoofSecurityModel}.
	 */
	private final String securityName;

	/**
	 * {@link HttpSecuritySource} class name.
	 */
	private final String httpSecuritySourceClassName;

	/**
	 * Timeout of the authentication.
	 */
	private final long authenticationTimeout;

	/**
	 * Content types.
	 */
	private final String[] contentTypes;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType;

	/**
	 * {@link WoofSecurityModel}.
	 */
	private final WoofSecurityModel securityModel;

	/**
	 * Mapping of {@link HttpSecurityType} output names to
	 * {@link WoofSecurityOutputModel} name.
	 */
	private final Map<String, String> outputNameMapping;

	/**
	 * Initiate for public use.
	 * 
	 * @param securityName
	 *            Name of the {@link WoofSecurityModel}.
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param authenticationTimeout
	 *            Authentication timeout.
	 * @param contentTypes
	 *            Content types.
	 */
	public SecurityInstance(String securityName, String httpSecuritySourceClassName, long authenticationTimeout,
			String[] contentTypes) {
		this.securityName = securityName;
		this.httpSecuritySourceClassName = httpSecuritySourceClassName;
		this.authenticationTimeout = authenticationTimeout;
		this.contentTypes = contentTypes;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.httpSecurityType = null;
		this.securityModel = null;
		this.outputNameMapping = null;
	}

	/**
	 * Initiate for public use from {@link WoofSecurityModel}.
	 * 
	 * @param model
	 *            {@link WoofSecurityModel}.
	 */
	public SecurityInstance(WoofSecurityModel model) {
		this.securityName = model.getHttpSecurityName();
		this.httpSecuritySourceClassName = model.getHttpSecuritySourceClassName();
		this.authenticationTimeout = model.getTimeout();
		this.httpSecurityType = null;
		this.securityModel = model;
		this.outputNameMapping = null;

		// Load the content types
		List<String> contentTypes = new LinkedList<>();
		for (WoofSecurityContentTypeModel contentType : model.getContentTypes()) {
			contentTypes.add(contentType.getContentType());
		}
		this.contentTypes = contentTypes.toArray(new String[contentTypes.size()]);

		// Load the properties
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : model.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(property.getValue());
		}
	}

	/**
	 * Initiate from {@link HttpSecuritySourceInstance}.
	 * 
	 * @param securityName
	 *            Name of the {@link WoofSecurityModel}.
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param authenticationTimeout
	 *            Authentication timeout.
	 * @param contentTypes
	 *            Content types.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 * @param outputNameMapping
	 *            Mapping of {@link HttpSecurityType} output name to
	 *            {@link WoofSecurityOutputModel} name.
	 */
	SecurityInstance(String securityName, String httpSecuritySourceClassName, long authenticationTimeout,
			String[] contentTypes, PropertyList propertyList, HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType,
			Map<String, String> outputNameMapping) {
		this.securityName = securityName;
		this.httpSecuritySourceClassName = httpSecuritySourceClassName;
		this.authenticationTimeout = authenticationTimeout;
		this.contentTypes = contentTypes;
		this.propertyList = propertyList;
		this.httpSecurityType = httpSecurityType;
		this.securityModel = null;
		this.outputNameMapping = outputNameMapping;
	}

	/**
	 * Obtains the name of the {@link WoofSecurityModel}.
	 * 
	 * @return Name of the {@link WoofSecurityModel}.
	 */
	public String getSecurityName() {
		return this.securityName;
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
	 * Obtains the <code>Content-Type</code> instances.
	 * 
	 * @return <code>Content-Type</code> instances.
	 */
	public String[] getContentTypes() {
		return this.contentTypes;
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
	 *         {@link HttpSecuritySourceInstance} or <code>null</code> if initiated
	 *         by <code>public</code> constructor.
	 */
	public HttpSecurityType<?, ?, ?, ?, ?> getHttpSecurityType() {
		return this.httpSecurityType;
	}

	/**
	 * Obtains the {@link WoofSecurityModel}.
	 * 
	 * @return {@link WoofSecurityModel}.
	 */
	public WoofSecurityModel getWoofSecurityModel() {
		return this.securityModel;
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