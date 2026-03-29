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

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofTemplateExtension;

/**
 * Context for {@link WoofTemplateExtensionSource} creating a {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionChangeContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the old {@link WoofTemplateExtensionConfiguration}.
	 * <p>
	 * Should the {@link WoofTemplateExtension} be added, then this will be
	 * <code>null</code>.
	 * 
	 * @return Old {@link WoofTemplateExtensionConfiguration}. May be
	 *         <code>null</code>.
	 */
	WoofTemplateExtensionConfiguration getOldConfiguration();

	/**
	 * <p>
	 * Obtains the new {@link WoofTemplateExtensionConfiguration}.
	 * <p>
	 * Should the {@link WoofTemplateExtension} be removed, then this will be
	 * <code>null</code>.
	 * 
	 * @return New {@link WoofTemplateExtensionConfiguration}. May be
	 *         <code>null</code>.
	 */
	WoofTemplateExtensionConfiguration getNewConfiguration();

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
	 * Obtains the {@link WoofChangeIssues} to report issues in
	 * applying/reverting a {@link Change}.
	 * 
	 * @return {@link WoofChangeIssues}.
	 */
	WoofChangeIssues getWoofChangeIssues();

}
