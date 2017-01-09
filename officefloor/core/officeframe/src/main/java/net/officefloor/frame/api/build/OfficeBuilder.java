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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.OfficeClock;
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
	 * <p>
	 * Allows providing an {@link OfficeClock} implementation to obtain the
	 * current time.
	 * <p>
	 * Should no {@link OfficeClock} be provided, a default implementation will
	 * be used.
	 * <p>
	 * Typically this is useful in testing to fix to a deterministic time.
	 * However, should there be native implementations of keeping time that is
	 * efficient, this enables overriding the default implementation.
	 * 
	 * @param clock
	 *            {@link OfficeClock}.
	 */
	void setOfficeClock(OfficeClock clock);

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
	 * Registers a {@link Team} which will execute {@link ManagedFunction}
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
	void setDefaultTeam(String officeTeamName);

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
	 *            Name to link the {@link ManagedObject} for
	 *            {@link ManagedFunction} instances.
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
	 *            Name to link the {@link ManagedObject} for
	 *            {@link ManagedFunction} instances.
	 * @param officeManagedObjectName
	 *            Name of the{@link ManagedObject} registered within this
	 *            {@link Office}.
	 * @return {@link DependencyMappingBuilder} to build any necessary
	 *         dependencies for the {@link ManagedObject}. See scope above.
	 */
	DependencyMappingBuilder addThreadManagedObject(String threadManagedObjectName, String officeManagedObjectName);

	/**
	 * Flags whether to manually manage {@link Governance} via
	 * {@link Administrator} instances.
	 * 
	 * @param isManuallyManageGovernance
	 *            <code>true</code> to manually manage {@link Governance} via
	 *            {@link Administrator} instances.
	 */
	void setManuallyManageGovernance(boolean isManuallyManageGovernance);

	/**
	 * Adds {@link Governance} within the {@link Office}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            Flow key type.
	 * @param governanceName
	 *            Name of the {@link Governance} to be referenced locally by
	 *            this {@link Office}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param governanceFactory
	 *            {@link GovernanceFactory} class.
	 * @return {@link GovernanceBuilder}.
	 */
	<E, F extends Enum<F>> GovernanceBuilder<F> addGovernance(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory);

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
	 * @param <E>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} key type.
	 * @param <AS>
	 *            {@link AdministratorSource} type.
	 * @param administratorName
	 *            Name to link the {@link Administrator} for the
	 *            {@link ManagedFunction} instances.
	 * @param adminsistratorSource
	 *            {@link AdministratorSource} class.
	 * @return administratorBuilder Builder of the {@link Administrator}.
	 */
	<E, A extends Enum<A>, AS extends AdministratorSource<E, A>> AdministratorBuilder<A> addAdministrator(
			String administratorName, Class<AS> adminsistratorSource);

	/**
	 * Adds a {@link ManagedFunction} to be executed within the {@link Office}.
	 * 
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param mangedFunctionFactory
	 *            {@link ManagedFunctionFactory} to create the
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	<O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> addManagedFunction(String functionName,
			ManagedFunctionFactory<O, F> mangedFunctionFactory);

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
	 * Adds an {@link EscalationFlow} for issues not handled within the
	 * {@link Office}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param functionName
	 *            Name of {@link ManagedFunction} to handle the
	 *            {@link EscalationFlow}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String functionName);

	/**
	 * Adds a {@link ManagedFunction} to invoke on start up of the
	 * {@link Office}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction}.
	 */
	void addStartupFunction(String functionName);

	/**
	 * Allows to optionally specify a {@link Profiler} that listens in on
	 * profiling information of the {@link Office}.
	 * 
	 * @param profiler
	 *            {@link Profiler}.
	 */
	void setProfiler(Profiler profiler);

}