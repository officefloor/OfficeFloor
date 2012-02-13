/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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