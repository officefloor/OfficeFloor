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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;

/**
 * Instance of a {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateExtensionInstance {

	/**
	 * Class name of the {@link WoofTemplateExtensionSource}.
	 */
	private String extensionSourceClassName;

	/**
	 * {@link PropertyList} for configuring the
	 * {@link WoofTemplateExtensionSource}.
	 */
	private PropertyList properties;

	/**
	 * Initiate.
	 * 
	 * @param extensionSourceClassName
	 *            Class name of the {@link WoofTemplateExtensionSource}.
	 * @param properties
	 *            {@link PropertyList} for configuring the
	 *            {@link WoofTemplateExtensionSource}.
	 */
	public HttpTemplateExtensionInstance(String extensionSourceClassName,
			PropertyList properties) {
		this.extensionSourceClassName = extensionSourceClassName;
		this.properties = properties;
	}

	/**
	 * Obtains the class name of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @return {@link WoofTemplateExtensionSource}.
	 */
	public String getTemplateExtensionClassName() {
		return this.extensionSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @return {@link PropertyList} to configure the
	 *         {@link WoofTemplateExtensionSource}.
	 */
	public PropertyList getProperties() {
		return this.properties;
	}

}