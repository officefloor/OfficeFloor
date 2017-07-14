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
package net.officefloor.model.woof;

import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link WoofTemplateChangeContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateChangeContextImpl extends SourceContextImpl implements WoofTemplateChangeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param issues
	 *            {@link WoofChangeIssues}.
	 * @param resourceSources
	 *            {@link ResourceSource} instances.
	 */
	public WoofTemplateChangeContextImpl(boolean isLoadingType, ClassLoader classLoader,
			ConfigurationContext configurationContext, WoofChangeIssues issues, ResourceSource... resourceSources) {
		super(isLoadingType, classLoader, resourceSources);
		this.configurationContext = configurationContext;
		this.issues = issues;
	}

	/*
	 * =============== WoofTemplateChangeContext =======================
	 */

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public WoofChangeIssues getWoofChangeIssues() {
		return this.issues;
	}

}