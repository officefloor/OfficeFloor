/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile;

import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Context for compiling the
 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
/**
 * @author Daniel
 *
 */
public class OfficeFloorCompilerContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link ModelRepositoryImpl}.
	 */
	private final ModelRepositoryImpl modelRepository;

	/**
	 * {@link LoaderContext}.
	 */
	private final LoaderContext loaderContext;

	/**
	 * Initiate.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param modelRepository
	 *            {@link ModelRepositoryImpl}.
	 * @param loaderContext
	 *            {@link LoaderContext}.
	 */
	public OfficeFloorCompilerContext(
			ConfigurationContext configurationContext,
			ModelRepositoryImpl modelRepository, 
			LoaderContext loaderContext) {
		this.configurationContext = configurationContext;
		this.modelRepository = modelRepository;
		this.loaderContext = loaderContext;
	}

	/**
	 * Obtains the {@link ConfigurationContext}.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	/**
	 * Obtains the {@link ModelRepositoryImpl}.
	 * 
	 * @return {@link ModelRepositoryImpl}.
	 */
	public ModelRepositoryImpl getModelRepository() {
		return this.modelRepository;
	}

	/**
	 * Obtains the {@link LoaderContext}.
	 * 
	 * @return {@link LoaderContext}.
	 */
	public LoaderContext getLoaderContext() {
		return this.loaderContext;
	}

}
