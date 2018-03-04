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
package net.officefloor.woof;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.woof.objects.WoofObjectsLoader;

/**
 * Context for the {@link WoofObjectsLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link WebArchitect}.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Obtains the {@link HttpSecurityArchitect}.
	 * 
	 * @return {@link HttpSecurityArchitect}.
	 */
	HttpSecurityArchitect getHttpSecurityArchitect();

	/**
	 * Obtains the {@link WebTemplateArchitect}.
	 * 
	 * @return {@link WebTemplateArchitect}.
	 */
	WebTemplateArchitect getWebTemplater();

	/**
	 * Obtains the {@link HttpResourceArchitect}.
	 * 
	 * @return {@link HttpResourceArchitect}.
	 */
	HttpResourceArchitect getHttpResourceArchitect();

	/**
	 * Obtains the {@link OfficeArchitect}.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}