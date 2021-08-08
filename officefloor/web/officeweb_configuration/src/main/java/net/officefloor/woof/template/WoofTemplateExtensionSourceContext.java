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

package net.officefloor.woof.template;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Context for the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * Obtains the application path to the {@link WebTemplate}.
	 * 
	 * @return Application path to the {@link WebTemplate}.
	 */
	String getApplicationPath();

	/**
	 * Obtains the {@link WebTemplate} being extended.
	 * 
	 * @return {@link WebTemplate} being extended.
	 */
	WebTemplate getTemplate();

	/**
	 * Obtains the {@link WebArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Obtains the {@link OfficeArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

}
