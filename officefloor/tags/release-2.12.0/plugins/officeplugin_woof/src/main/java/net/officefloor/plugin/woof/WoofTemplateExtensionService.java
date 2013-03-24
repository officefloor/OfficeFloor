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
package net.officefloor.plugin.woof;

import java.util.ServiceLoader;

import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;

/**
 * {@link ServiceLoader} service that allows overriding default behaviour of
 * adding a {@link HttpTemplateAutoWireSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionService {

	/**
	 * Obtains the alias for this {@link HttpTemplateAutoWireSectionExtension}.
	 * 
	 * @return Alias for this {@link HttpTemplateAutoWireSectionExtension}.
	 */
	String getTemplateExtensionAlias();

	/**
	 * <p>
	 * An implicit extension does not require explicit configuration.
	 * <p>
	 * Should a {@link WoofTemplateExtensionService} be implicit it will extend
	 * every {@link WoofTemplateModel} and unless explicitly configured will
	 * have no property values. Therefore all implicit
	 * {@link WoofTemplateExtensionService} instances must be able configure
	 * themselves without properties (from the
	 * {@link WoofTemplateExtensionServiceContext} declared configuration).
	 * 
	 * @return <code>true</code> if implicit extension.
	 */
	boolean isImplicitExtension();

	/**
	 * Extends the {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param context
	 *            {@link WoofTemplateExtensionServiceContext}.
	 * @throws Exception
	 *             If fails to extend the {@link HttpTemplateAutoWireSection}.
	 */
	void extendTemplate(WoofTemplateExtensionServiceContext context)
			throws Exception;

}