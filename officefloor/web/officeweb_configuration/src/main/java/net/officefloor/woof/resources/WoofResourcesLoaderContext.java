/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof.resources;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.resource.build.HttpResourceArchitect;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * resources.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         resources.
	 */
	ConfigurationItem getConfiguration();

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
