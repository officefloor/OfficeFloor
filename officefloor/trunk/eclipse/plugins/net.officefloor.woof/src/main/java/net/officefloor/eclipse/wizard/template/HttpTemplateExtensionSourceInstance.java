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

import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtension;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;

/**
 * Instance of the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateExtensionSourceInstance {

	/**
	 * Class name of the {@link WoofTemplateExtensionSource}.
	 */
	private final String woofTemplateExtensionSourceClassName;

	/**
	 * {@link WoofTemplateExtensionSourceExtension} for the
	 * {@link WoofTemplateExtensionSource}.
	 */
	private final WoofTemplateExtensionSourceExtension<?> extension;

	/**
	 * {@link HttpTemplateExtensionSourceInstanceContext}.
	 */
	private final HttpTemplateExtensionSourceInstanceContext context;

	/**
	 * Initiate.
	 * 
	 * @param woofTemplateExtensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param extension
	 *            {@link WoofTemplateExtensionSourceExtension}. May be
	 *            <code>null</code>.
	 * @param context
	 *            {@link HttpTemplateExtensionSourceInstanceContext}.
	 */
	public HttpTemplateExtensionSourceInstance(
			String woofTemplateExtensionSourceClassName,
			WoofTemplateExtensionSourceExtension<?> extension,
			HttpTemplateExtensionSourceInstanceContext context) {
		this.woofTemplateExtensionSourceClassName = woofTemplateExtensionSourceClassName;
		this.extension = extension;
		this.context = context;
	}

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} class name.
	 * 
	 * @return {@link WoofTemplateExtensionSource} class name.
	 */
	public String getWoofTemplateExtensionSourceClassName() {
		return this.woofTemplateExtensionSourceClassName;
	}

}