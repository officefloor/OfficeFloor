/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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