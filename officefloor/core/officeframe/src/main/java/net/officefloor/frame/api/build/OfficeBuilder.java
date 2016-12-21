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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Builder of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeBuilder {

	/**
	 * Specifies the interval in milli-seconds between each time the
	 * {@link OfficeManager} monitors the {@link Office}.
	 * 
	 * @param monitorOfficeInterval
	 *            Interval in milli-seconds between each time the
	 *            {@link OfficeManager} monitors the {@link Office}.
	 */
	void setMonitorOfficeInterval(long monitorOfficeInterval);

	/**
	 * Registers a {@link Team} which will execute {@link FunctionState}
	 * instances within this {@link Office}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link Team} to be referenced locally by this
	 *            {@link Office}.
	 * @param officeFloorTeamName
	 *            Name of the {@link Team} within the {@link OfficeFloor}.
	 */
	void registerTeam(String officeTeamName, String officeFloorTeamName);

	/**
	 * Allows optionally specifying the default {@link Team} for the
	 * {@link Office}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link Team} within this {@link Office} to use as
	 *            default {@link Team}.
	 */
	void setDefaultTeamName(String officeTeamName);

	/**
	 * Registers the {@link ManagedObject} within this {@link Office}.
	 * 
	 * @param officeManagedObjectName
	 *            Name of the {@link ManagedObject} to be referenced locally by
	 *            this {@link Office}.
	 * @param officeFloorManagedObjectSourceName
	 *            Name of the {@link ManagedObjectSource} within the
	 *            {@link OfficeFloor}.
	 */
	void registerManagedObjectSource(String officeManagedObjectName, String officeFloorManagedObjectSourceName);

	/**
	 * Specifies the input {@link ManagedObject} to be bound to the
	 * {@link ProcessState} should there be multiple instances and it was not
	 * input.
	 * 
	 * @param inputManagedObjectName
	 *            Input {@link ManagedObject} name.
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} name.
	 */
	void setBoundInputManagedObject(String inputManagedObjectName, String managedObjectSourceName);

	/**
	 * <p>
	 * Adds a {@link ProcessState} bound {@link ManagedObject} to this
	 * {@link Office}.
	 * <p>
	 * Dependency scope:
	 * <ol>
	 * <li>Other {@link ManagedObject} instances added via this method.</li>
	 * </ol>
	 * 
	 * @param processManagedObjectName
	 *            Name to link the {@link ManagedObject} into {@link Work}.
	 * @param officeManagedObjectName
	 *            Name of the {@link ManagedObject} registered within this
	 *            {@link Office}.
	 * @return {@link DependencyMappingBuilder} to build any necessary
	 *         dependencies for the {@link ManagedObject}. See scope above.
	 */
	DependencyMappingBuilder addProcessManagedObject(String processManagedObjectName, String officeManagedObjectName);

	/**
	 * <p>
	 * Adds a {@link ThreadState} bound {@link ManagedObject} to this
	 * {@link Office}.
	 * <p>
	 * Dependency scope:
	 * <ol>
	 * <li>Other {@link ManagedObject} instances added via this method.</li>
	 * <li>{@link ProcessState} bound {@link ManagedObject} instances.</li>
	 * </ol>
	 * 
	 * @param threadManagedObjectName
	 *            Name to link the {@link ManagedObject} into {@link Work}.
	 * @param officeManagedObjectName
	 *            Name of the{@link ManagedObject} registered within this
	 *            {@link Office}.
	 * @return {@link DependencyMappingBuilder} to build any necessary
	 *         dependencies for the {@link ManagedObject}. See scope above.
	 */
	DependencyMappingBuilder addThreadManagedObject(String threadManagedObjectName, String officeManagedObjectName);

	/**
	 * <p>
	 * Flags to manually manage the {@link Governance}.
	 * <p>
	 * WARNING: given the nuances of {@link Task} completion be wary of
	 * attempting to manually manage the {@link Governance}.
	 * <p>
	 * Manually managing however is useful for multi-threaded execution and
	 * managing {@link Governance}.
	 * 
	 * @param isManuallyManage
	 *            <code>true</code> to manually manage.
	 */
	void setManuallyManageGovernance(boolean isManuallyManage);

	/**
	 * Adds {@link Governance} within the {@link Office}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <F>
	 *            Flow key type.
	 * @param governanceName
	 *            Name of the {@link Governance} to be referenced locally by
	 *            this {@link Office}.
	 * @param governanceFactory
	 *            {@link GovernanceFactory} class.
	 * @param extensionInterface
	 *            Extension interface.
	 * @return {@link GovernanceBuilder}.
	 */
	<I, F extends Enum<F>> GovernanceBuilder<F> addGovernance(String governanceName,
			GovernanceFactory<? super I, F> governanceFactory, Class<I> extensionInterface);

	/**
	 * <p>
	 * Adds a {@link ProcessState} bound {@link AdministratorSource} to this
	 * {@link OfficeBuilder}.
	 * <p>
	 * Dependency scope for administered {@link ManagedObject} instances:
	 * <ol>
	 * <li>{@link ProcessState} bound {@link ManagedObject} instances.</li>
	 * </ol>
	 * 
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} key type.
	 * @param <AS>
	 *            {@link AdministratorSource} type.
	 * @param processAdministratorName
	 *            Name to link the {@link Administrator} into {@link Work}.
	 * @param adminsistratorSource
	 *            {@link AdministratorSource} class.
	 * @return {@link AdministratorBuilder} for the {@link Administrator}.
	 */
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addProcessAdministrator(
			String processAdministratorName, Class<AS> adminsistratorSource);

	/**
	 * <p>
	 * Adds a {@link ThreadState} bound {@link AdministratorSource} to this
	 * {@link OfficeBuilder}.
	 * <p>
	 * Dependency scope for administered {@link ManagedObject} instances:
	 * <ol>
	 * <li>{@link ThreadState} bound {@link ManagedObject} instances.</li>
	 * <li>{@link ProcessState} bound {@link ManagedObject} instances.</li>
	 * </ol>
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} key type.
	 * @param <AS>
	 *            {@link AdministratorSource} type.
	 * @param threadAdministratorName
	 *            Name to link the {@link Administrator} into {@link Work}.
	 * @param adminsistratorSource
	 *            {@link AdministratorSource} class.
	 * @return administratorBuilder Builder of the {@link Administrator}.
	 */
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addThreadAdministrator(
			String threadAdministratorName, Class<AS> adminsistratorSource);

	/**
	 * Adds {@link Work} to be done within this {@link Office}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param workName
	 *            Name identifying the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory} to create the {@link Work}.
	 * @return {@link WorkBuilder} to build the {@link Work}.
	 */
	<W extends Work> WorkBuilder<W> addWork(String workName, WorkFactory<W> workFactory);

	/**
	 * <p>
	 * Adds {@link OfficeEnhancer} for this {@link Office}.
	 * <p>
	 * This enables enhancing the {@link Office} after the
	 * {@link ManagedObjectSource} instances are registered.
	 * 
	 * @param officeEnhancer
	 *            {@link OfficeEnhancer}.
	 */
	void addOfficeEnhancer(OfficeEnhancer officeEnhancer);

	/**
	 * Adds an {@link EscalationFlow} for issues not handled by the {@link Flow}
	 * of the {@link Office}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} on the {@link Work} to handle the
	 *            {@link EscalationFlow}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String workName, String taskName);

	/**
	 * Adds a {@link Task} to invoke on start up of the {@link Office}.
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link Task}.
	 * @param taskName
	 *            Name of {@link Task} on the {@link Work}.
	 */
	void addStartupTask(String workName, String taskName);

	/**
	 * Allows to optionally specify a {@link Profiler} that listens in on
	 * profiling information of the {@link Office}.
	 * 
	 * @param profiler
	 *            {@link Profiler}.
	 */
	void setProfiler(Profiler profiler);

}