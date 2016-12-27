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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeConfiguration {

	/**
	 * Obtains the name of this {@link Office}.
	 * 
	 * @return Name of this {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains the interval in milli-seconds between each time the
	 * {@link OfficeManager} monitors the {@link Office}.
	 * 
	 * @return Interval in milli-seconds between each time the
	 *         {@link OfficeManager} monitors the {@link Office}.
	 */
	long getMonitorOfficeInterval();

	/**
	 * Obtains the default {@link Team} name for the {@link Office}.
	 * 
	 * @return Default {@link Team} name for the {@link Office}. May be
	 *         <code>null</code> to use any {@link Team} (typically the invoking
	 *         {@link Thread}).
	 */
	String getOfficeDefaultTeamName();

	/**
	 * <p>
	 * Obtains the {@link OfficeBuilder} for this {@link Office}.
	 * <p>
	 * This is to allow {@link Asset} instances (such as a
	 * {@link ManagedObjectSource}) to provide additional configuration for the
	 * {@link Office}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	OfficeBuilder getBuilder();

	/**
	 * Obtains the links to the {@link OfficeFloor} {@link Team} instances.
	 * 
	 * @return Links to the {@link OfficeFloor} {@link Team} instances.
	 */
	LinkedTeamConfiguration[] getRegisteredTeams();

	/**
	 * Obtains the links to the {@link OfficeFloor} {@link ManagedObjectSource}
	 * instances.
	 * 
	 * @return Links to the {@link OfficeFloor} {@link ManagedObjectSource}
	 *         instances.
	 */
	LinkedManagedObjectSourceConfiguration[] getRegisteredManagedObjectSources();

	/**
	 * Obtains the configuration of the {@link ManagedObjectSource} to be bound
	 * for the input {@link ManagedObject}.
	 * 
	 * @return Configuration of the {@link ManagedObjectSource} to be bound for
	 *         the input {@link ManagedObject}.
	 */
	BoundInputManagedObjectConfiguration[] getBoundInputManagedObjectConfiguration();

	/**
	 * Obtains the {@link ManagedObject} instances to be bound to the
	 * {@link ProcessState} of this {@link Office}.
	 * 
	 * @return Listing of the configuration of the {@link ManagedObject}
	 *         instances bound to the {@link ProcessState}.
	 */
	ManagedObjectConfiguration<?>[] getProcessManagedObjectConfiguration();

	/**
	 * Obtains the {@link ManagedObject} instances to be bound to the
	 * {@link ThreadState} of this {@link Office}.
	 * 
	 * @return Listing of the configuration of the {@link ManagedObject}
	 *         instances bound to the {@link ThreadState}.
	 */
	ManagedObjectConfiguration<?>[] getThreadManagedObjectConfiguration();

	/**
	 * Obtains the {@link GovernanceConfiguration}.
	 * 
	 * @return {@link GovernanceConfiguration}.
	 */
	GovernanceConfiguration<?, ?>[] getGovernanceConfiguration();

	/**
	 * Obtains the configuration of the {@link AdministratorSource} instances
	 * bound to the {@link ProcessState}.
	 * 
	 * @return {@link AdministratorSource} configuration of instances bound to
	 *         the {@link ProcessState}.
	 */
	AdministratorSourceConfiguration<?, ?>[] getProcessAdministratorSourceConfiguration();

	/**
	 * Obtains the configuration of the {@link AdministratorSource} instances
	 * bound to the {@link ThreadState}.
	 * 
	 * @return {@link AdministratorSource} configuration of instances bound to
	 *         the {@link ThreadState}.
	 */
	AdministratorSourceConfiguration<?, ?>[] getThreadAdministratorSourceConfiguration();

	/**
	 * Obtains the configuration for the {@link ManagedFunction} instances.
	 * 
	 * @return Configuration for the {@link ManagedFunction} instances.
	 */
	ManagedFunctionConfiguration<?, ?>[] getManagedFunctionConfiguration();

	/**
	 * Obtains the {@link OfficeEnhancer} instances for this {@link Office}.
	 * 
	 * @return Listing of the {@link OfficeEnhancer} for this {@link Office}.
	 */
	OfficeEnhancer[] getOfficeEnhancers();

	/**
	 * Obtains the {@link ManagedFunctionEscalationConfiguration} instances for
	 * the {@link Office}.
	 * 
	 * @return {@link ManagedFunctionEscalationConfiguration} instances for the
	 *         {@link Office}.
	 */
	ManagedFunctionEscalationConfiguration[] getEscalationConfiguration();

	/**
	 * <p>
	 * Obtains a {@link FlowNodeBuilder} registered with this
	 * {@link OfficeBuilder}.
	 * <p>
	 * This enables addition configuration of {@link ManagedFunction} instances
	 * registered by a {@link ManagedObjectSource}.
	 * 
	 * @param namespace
	 *            Namespace. Likely the {@link ManagedObjectSource} name.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String namespace, String functionName);

	/**
	 * Obtains the list of {@link ManagedFunctionReference} instances
	 * referencing the {@link ManagedFunction} instances to invoke on
	 * {@link Office} start up.
	 * 
	 * @return List of start up {@link ManagedFunctionReference} references.
	 */
	ManagedFunctionReference[] getStartupFunctions();

	/**
	 * Obtains the {@link Profiler} for the {@link Office}.
	 * 
	 * @return {@link Profiler} for the {@link Office}.
	 */
	Profiler getProfiler();

}