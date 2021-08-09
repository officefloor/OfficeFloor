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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.FlowBuilder;
import net.officefloor.frame.api.build.FunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

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
	 * Obtains the {@link MonitorClock}.
	 * 
	 * @return {@link MonitorClock}. May be <code>null</code> to use a default
	 *         implementation.
	 */
	MonitorClock getMonitorClock();

	/**
	 * Obtains the interval in milli-seconds between each time the
	 * {@link OfficeManager} monitors the {@link Office}.
	 * 
	 * @return Interval in milli-seconds between each time the {@link OfficeManager}
	 *         monitors the {@link Office}.
	 */
	long getMonitorOfficeInterval();

	/**
	 * Obtains the maximum {@link FunctionState} chain length.
	 * 
	 * @return Maximum {@link FunctionState} chain length.
	 */
	int getMaximumFunctionStateChainLength();

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
	 * Obtains the configuration of the {@link ManagedObjectSource} to be bound for
	 * the input {@link ManagedObject}.
	 * 
	 * @return Configuration of the {@link ManagedObjectSource} to be bound for the
	 *         input {@link ManagedObject}.
	 */
	BoundInputManagedObjectConfiguration[] getBoundInputManagedObjectConfiguration();

	/**
	 * Obtains the {@link ManagedObject} instances to be bound to the
	 * {@link ProcessState} of this {@link Office}.
	 * 
	 * @return Listing of the configuration of the {@link ManagedObject} instances
	 *         bound to the {@link ProcessState}.
	 */
	ManagedObjectConfiguration<?>[] getProcessManagedObjectConfiguration();

	/**
	 * Obtains the {@link ManagedObject} instances to be bound to the
	 * {@link ThreadState} of this {@link Office}.
	 * 
	 * @return Listing of the configuration of the {@link ManagedObject} instances
	 *         bound to the {@link ThreadState}.
	 */
	ManagedObjectConfiguration<?>[] getThreadManagedObjectConfiguration();

	/**
	 * Obtains the {@link GovernanceConfiguration}.
	 * 
	 * @return {@link GovernanceConfiguration}.
	 */
	GovernanceConfiguration<?, ?>[] getGovernanceConfiguration();

	/**
	 * Indicates if manually managing {@link Governance} via {@link Administration}.
	 * 
	 * @return <code>true</code> to manually managed {@link Governance}.
	 */
	boolean isManuallyManageGovernance();

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
	 * Obtains the {@link EscalationConfiguration} instances for the {@link Office}.
	 * 
	 * @return {@link EscalationConfiguration} instances for the {@link Office}.
	 */
	EscalationConfiguration[] getEscalationConfiguration();

	/**
	 * <p>
	 * Obtains a {@link FunctionBuilder} registered with this {@link OfficeBuilder}.
	 * <p>
	 * This enables addition configuration of {@link ManagedFunction} instances
	 * registered by a {@link ManagedObjectSource}.
	 * 
	 * @param namespace    Namespace. Likely the {@link ManagedObjectSource} name.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String namespace, String functionName);

	/**
	 * Obtains the list of {@link ManagedFunctionInvocation} instances referencing
	 * the {@link ManagedFunction} instances to invoke on {@link Office} start up.
	 * 
	 * @return List of start up {@link ManagedFunctionInvocation} references.
	 */
	ManagedFunctionInvocation[] getStartupFunctions();

	/**
	 * Obtains the {@link Profiler} for the {@link Office}.
	 * 
	 * @return {@link Profiler} for the {@link Office}.
	 */
	Profiler getProfiler();

	/**
	 * Obtains the {@link ThreadSynchroniserFactory} instances to synchronise the
	 * {@link ThreadLocal} state between {@link Team} instances.
	 * 
	 * @return {@link ThreadSynchroniserFactory} instances.
	 */
	ThreadSynchroniserFactory[] getThreadSynchronisers();

	/**
	 * Obtains the default {@link AsynchronousFlow} timeout for
	 * {@link AsynchronousFlow} instances instigated by the {@link Office}.
	 * 
	 * @return Default {@link AsynchronousFlow} timeout for {@link AsynchronousFlow}
	 *         instances instigated by the {@link Office}.
	 */
	long getDefaultAsynchronousFlowTimeout();

}
