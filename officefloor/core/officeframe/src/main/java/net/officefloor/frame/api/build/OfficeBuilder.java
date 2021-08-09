/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Builder of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeBuilder {

	/**
	 * <p>
	 * Allows providing an {@link MonitorClock} implementation to obtain the current
	 * time.
	 * <p>
	 * Should no {@link MonitorClock} be provided, a default implementation will be
	 * used.
	 * <p>
	 * Typically this is useful in testing to fix to a deterministic time. However,
	 * should there be native implementations of keeping time that is efficient,
	 * this enables overriding the default implementation.
	 * 
	 * @param clock {@link MonitorClock}.
	 */
	void setMonitorClock(MonitorClock clock);

	/**
	 * Specifies the interval in milli-seconds between each time the
	 * {@link OfficeManager} monitors the {@link Office}.
	 * 
	 * @param monitorOfficeInterval Interval in milli-seconds between each time the
	 *                              {@link OfficeManager} monitors the
	 *                              {@link Office}.
	 */
	void setMonitorOfficeInterval(long monitorOfficeInterval);

	/**
	 * <p>
	 * Specifies the maximum {@link FunctionState} chain length.
	 * <p>
	 * This value is a trade off between limiting {@link Thread} stack calls and
	 * performance. Setting this value low runs the risk of
	 * {@link StackOverflowError} occurring in having recursively call into the
	 * {@link FunctionState} chain. Setting this value high, has more {@link Thread}
	 * overheads in breaking the recursive chain, slowing performance.
	 * 
	 * @param maximumFunctionStateChainLength Maximum {@link FunctionState} chain
	 *                                        length.
	 */
	void setMaximumFunctionStateChainLength(int maximumFunctionStateChainLength);

	/**
	 * Specifies the default timeout for {@link AsynchronousFlow} instances.
	 *
	 * @param timeout Default timeout.
	 */
	void setDefaultAsynchronousFlowTimeout(long timeout);

	/**
	 * Registers a {@link Team} which will execute {@link ManagedFunction} instances
	 * within this {@link Office}.
	 * 
	 * @param officeTeamName      Name of the {@link Team} to be referenced locally
	 *                            by this {@link Office}.
	 * @param officeFloorTeamName Name of the {@link Team} within the
	 *                            {@link OfficeFloor}.
	 */
	void registerTeam(String officeTeamName, String officeFloorTeamName);

	/**
	 * Allows optionally specifying the default {@link Team} for the {@link Office}.
	 * 
	 * @param officeTeamName Name of the {@link Team} within this {@link Office} to
	 *                       use as default {@link Team}.
	 */
	void setDefaultTeam(String officeTeamName);

	/**
	 * Registers the {@link ManagedObject} within this {@link Office}.
	 * 
	 * @param officeManagedObjectName            Name of the {@link ManagedObject}
	 *                                           to be referenced locally by this
	 *                                           {@link Office}.
	 * @param officeFloorManagedObjectSourceName Name of the
	 *                                           {@link ManagedObjectSource} within
	 *                                           the {@link OfficeFloor}.
	 */
	void registerManagedObjectSource(String officeManagedObjectName, String officeFloorManagedObjectSourceName);

	/**
	 * Specifies the input {@link ManagedObject} to be bound to the
	 * {@link ProcessState} should there be multiple instances and it was not input.
	 * 
	 * @param inputManagedObjectName  Input {@link ManagedObject} name.
	 * @param managedObjectSourceName {@link ManagedObjectSource} name.
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
	 * @param processManagedObjectName Name to link the {@link ManagedObject} for
	 *                                 {@link ManagedFunction} instances.
	 * @param officeManagedObjectName  Name of the {@link ManagedObject} registered
	 *                                 within this {@link Office}.
	 * @return {@link ThreadDependencyMappingBuilder} to build any necessary
	 *         dependencies for the {@link ManagedObject}. See scope above.
	 */
	ThreadDependencyMappingBuilder addProcessManagedObject(String processManagedObjectName,
			String officeManagedObjectName);

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
	 * @param threadManagedObjectName Name to link the {@link ManagedObject} for
	 *                                {@link ManagedFunction} instances.
	 * @param officeManagedObjectName Name of the{@link ManagedObject} registered
	 *                                within this {@link Office}.
	 * @return {@link ThreadDependencyMappingBuilder} to build any necessary
	 *         dependencies for the {@link ManagedObject}. See scope above.
	 */
	ThreadDependencyMappingBuilder addThreadManagedObject(String threadManagedObjectName,
			String officeManagedObjectName);

	/**
	 * Flags whether to manually manage {@link Governance} via
	 * {@link Administration} instances.
	 * 
	 * @param isManuallyManageGovernance <code>true</code> to manually manage
	 *                                   {@link Governance} via
	 *                                   {@link Administration} instances.
	 */
	void setManuallyManageGovernance(boolean isManuallyManageGovernance);

	/**
	 * Adds {@link Governance} within the {@link Office}.
	 * 
	 * @param <E>                Extension interface type.
	 * @param <F>                Flow key type.
	 * @param governanceName     Name of the {@link Governance} to be referenced
	 *                           locally by this {@link Office}.
	 * @param extensionInterface Extension interface.
	 * @param governanceFactory  {@link GovernanceFactory} class.
	 * @return {@link GovernanceBuilder}.
	 */
	<E, F extends Enum<F>> GovernanceBuilder<F> addGovernance(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory);

	/**
	 * Adds a {@link ManagedFunction} to be executed within the {@link Office}.
	 * 
	 * @param <O>                   Dependency key type.
	 * @param <F>                   Flow key type.
	 * @param functionName          Name of the {@link ManagedFunction}.
	 * @param mangedFunctionFactory {@link ManagedFunctionFactory} to create the
	 *                              {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	<O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> addManagedFunction(String functionName,
			ManagedFunctionFactory<O, F> mangedFunctionFactory);

	/**
	 * Adds an {@link EscalationFlow} for issues not handled within the
	 * {@link Office}.
	 * 
	 * @param typeOfCause  Type of cause handled by this {@link EscalationFlow}.
	 * @param functionName Name of {@link ManagedFunction} to handle the
	 *                     {@link EscalationFlow}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String functionName);

	/**
	 * Adds a {@link ManagedFunction} to invoke on start up of the {@link Office}.
	 * 
	 * @param functionName Name of {@link ManagedFunction}.
	 * @param parameter    Parameter value to be passed to the
	 *                     {@link ManagedFunction}. May be <code>null</code>.
	 */
	void addStartupFunction(String functionName, Object parameter);

	/**
	 * <p>
	 * Adds {@link OfficeEnhancer} for this {@link Office}.
	 * <p>
	 * This enables enhancing the {@link Office} after the
	 * {@link ManagedObjectSource} instances are registered.
	 * 
	 * @param officeEnhancer {@link OfficeEnhancer}.
	 */
	void addOfficeEnhancer(OfficeEnhancer officeEnhancer);

	/**
	 * Allows to optionally specify a {@link Profiler} that listens in on profiling
	 * information of the {@link Office}.
	 * 
	 * @param profiler {@link Profiler}.
	 */
	void setProfiler(Profiler profiler);

	/**
	 * Adds a {@link ThreadSynchroniser} for the {@link ThreadState} of the
	 * {@link Office}.
	 * 
	 * @param threadSynchroniserFactory {@link ThreadSynchroniserFactory} to create
	 *                                  the {@link ThreadSynchroniser}.
	 */
	void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory);

}
