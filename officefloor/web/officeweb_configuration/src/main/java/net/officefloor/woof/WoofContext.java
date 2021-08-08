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

package net.officefloor.woof;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.template.build.WebTemplateArchitect;

/**
 * Context for the {@link WoofLoader}.
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
	 * Obtains the {@link ProcedureArchitect}.
	 * 
	 * @return {@link ProcedureArchitect}.
	 */
	ProcedureArchitect<OfficeSection> getProcedureArchitect();

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
