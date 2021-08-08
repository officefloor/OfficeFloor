/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.teams;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.woof.resources.WoofResourcesLoader;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsUsageContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * teams.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link OfficeArchitect} to be configured with the teams.
	 * 
	 * @return {@link OfficeArchitect} to be configured.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}
