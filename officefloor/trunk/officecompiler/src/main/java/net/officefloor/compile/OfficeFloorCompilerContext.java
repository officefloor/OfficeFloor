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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.LoaderContext;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ModelRepository;

/**
 * Context for compiling the
 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorCompilerContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * {@link BuilderFactory}.
	 */
	private final BuilderFactory builderFactory;

	/**
	 * {@link LoaderContext}.
	 */
	private final LoaderContext loaderContext;

	/**
	 * Registry of the {@link WorkEntry}.
	 */
	private final Map<String, WorkEntry<?>> workRegistry = new HashMap<String, WorkEntry<?>>();

	/**
	 * Registry of the {@link TaskEntry}.
	 */
	private final Map<FlowItemModel, TaskEntry<?>> taskRegistry = new HashMap<FlowItemModel, TaskEntry<?>>();

	/**
	 * Registry of the {@link OfficeEntry}.
	 */
	private final Map<String, OfficeEntry> officeRegistry = new HashMap<String, OfficeEntry>();

	/**
	 * Registry of the {@link ManagedObjectSourceEntry}.
	 */
	private final Map<String, ManagedObjectSourceEntry> mosRegistry = new HashMap<String, ManagedObjectSourceEntry>();

	/**
	 * Registry of the {@link TeamEntry}.
	 */
	private final Map<String, TeamEntry> teamRegistry = new HashMap<String, TeamEntry>();

	/**
	 * Initiate.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 * @param builderFactory
	 *            {@link BuilderFactory}.
	 * @param loaderContext
	 *            {@link LoaderContext}.
	 */
	public OfficeFloorCompilerContext(
			ConfigurationContext configurationContext,
			ModelRepository modelRepository, BuilderFactory builderFactory,
			LoaderContext loaderContext) {
		this.configurationContext = configurationContext;
		this.modelRepository = modelRepository;
		this.builderFactory = builderFactory;
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
	 * Obtains the {@link ModelRepository}.
	 * 
	 * @return {@link ModelRepository}.
	 */
	public ModelRepository getModelRepository() {
		return this.modelRepository;
	}

	/**
	 * Obtains the {@link BuilderFactory}.
	 * 
	 * @return {@link BuilderFactory}.
	 */
	public BuilderFactory getBuilderFactory() {
		return this.builderFactory;
	}

	/**
	 * Obtains the {@link LoaderContext}.
	 * 
	 * @return {@link LoaderContext}.
	 */
	public LoaderContext getLoaderContext() {
		return this.loaderContext;
	}

	/**
	 * Obtains the registry of {@link WorkEntry} instances.
	 * 
	 * @return Registry of {@link WorkEntry} instances.
	 */
	public Map<String, WorkEntry<?>> getWorkRegistry() {
		return this.workRegistry;
	}

	/**
	 * Obtains the registry of {@link TaskEntry} instances.
	 * 
	 * @return Registry of {@link TaskEntry} instances.
	 */
	public Map<FlowItemModel, TaskEntry<?>> getTaskRegistry() {
		return this.taskRegistry;
	}

	/**
	 * Obtains the registry of {@link OfficeEntry} instances.
	 * 
	 * @return Registry of {@link OfficeEntry} instances.
	 */
	public Map<String, OfficeEntry> getOfficeRegistry() {
		return this.officeRegistry;
	}

	/**
	 * Obtains the registry of {@link ManagedObjectSourceEntry} instances.
	 * 
	 * @return Registry of {@link ManagedObjectSourceEntry} instances.
	 */
	public Map<String, ManagedObjectSourceEntry> getManagedObjectSourceRegistry() {
		return this.mosRegistry;
	}

	/**
	 * Obtains the registry of {@link TeamEntry} instances.
	 * 
	 * @return Registry of {@link TeamEntry} instances.
	 */
	public Map<String, TeamEntry> getTeamRegistry() {
		return this.teamRegistry;
	}
}
