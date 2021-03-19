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
