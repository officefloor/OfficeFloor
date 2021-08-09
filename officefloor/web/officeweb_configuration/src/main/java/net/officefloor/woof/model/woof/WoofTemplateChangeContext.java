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

package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.model.change.Change;

/**
 * Context for changing a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateChangeContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the {@link ConfigurationContext}.
	 * <p>
	 * The {@link ConfigurationContext} is at the root of the Project source.
	 * <p>
	 * Note that Projects are anticipated to follow the standard
	 * <a href="http://maven.apache.org">Maven</a> project structure.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	ConfigurationContext getConfigurationContext();

	/**
	 * Obtains the {@link WoofChangeIssues} to allow reporting issue in
	 * attempting the {@link Change}.
	 * 
	 * @return {@link WoofChangeIssues} to allow reporting issue in attempting
	 *         the {@link Change}.
	 */
	WoofChangeIssues getWoofChangeIssues();

}
