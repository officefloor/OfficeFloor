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
package net.officefloor.plugin.woof.template;

import java.util.ServiceLoader;

import net.officefloor.model.woof.WoofTemplateModel;

/**
 * {@link ServiceLoader} service for providing a
 * {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceService<S extends WoofTemplateExtensionSource> {

	/**
	 * <p>
	 * An implicit extension does not require explicit configuration.
	 * <p>
	 * Should a {@link WoofTemplateExtensionSource} be implicit it will extend
	 * every {@link WoofTemplateModel} and unless explicitly configured will
	 * have no property values. Therefore all implicit
	 * {@link WoofTemplateExtensionSource} instances must be able to configure
	 * themselves without properties (from the
	 * {@link WoofTemplateExtensionSourceContext} declared configuration).
	 * 
	 * @return <code>true</code> if implicit extension.
	 */
	boolean isImplicitExtension();

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} {@link Class}.
	 * 
	 * @return {@link WoofTemplateExtensionSource} {@link Class}.
	 */
	Class<S> getWoofTemplateExtensionSourceClass();

}