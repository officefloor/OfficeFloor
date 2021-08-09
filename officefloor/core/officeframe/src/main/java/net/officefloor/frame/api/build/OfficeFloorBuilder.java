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

import java.util.function.Consumer;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.internal.structure.FunctionState;

/**
 * Builder of an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorBuilder {

	/**
	 * Adds an active profile for the {@link OfficeFloor}.
	 * 
	 * @param profile Active profile for the {@link OfficeFloor}.
	 */
	void addProfile(String profile);

	/**
	 * Allows overriding the {@link ClassLoader} provided to the sources by the
	 * {@link SourceContext}.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 */
	void setClassLoader(ClassLoader classLoader);

	/**
	 * Allows overriding the {@link ClockFactory} to create {@link Clock} instances
	 * by the {@link SourceContext}.
	 * 
	 * @param clockFactory {@link ClockFactory}.
	 */
	void setClockFactory(ClockFactory clockFactory);

	/**
	 * Specifies the maximum time in milliseconds to wait for {@link OfficeFloor} to
	 * start.
	 * 
	 * @param maxStartupWaitTime Maximum time in milliseconds to wait for
	 *                           {@link OfficeFloor} to start.
	 */
	void setMaxStartupWaitTime(long maxStartupWaitTime);

	/**
	 * Decorates all the {@link Thread} instances created by the
	 * {@link TeamSourceContext}.
	 * 
	 * @param decorator Decorates all the {@link Thread} instances created by the
	 *                  {@link TeamSourceContext}.
	 */
	void setThreadDecorator(Consumer<Thread> decorator);

	/**
	 * Adds a {@link ResourceSource} to locate resources.
	 * 
	 * @param resourceSource {@link ResourceSource}.
	 */
	void addResources(ResourceSource resourceSource);

	/**
	 * Adds an {@link OfficeVisitor}.
	 * 
	 * @param visitor {@link OfficeVisitor}.
	 */
	void addOfficeVisitor(OfficeVisitor visitor);

	/**
	 * Adds an {@link OfficeFloorListener}.
	 * 
	 * @param listener {@link OfficeFloorListener}.
	 */
	void addOfficeFloorListener(OfficeFloorListener listener);

	/**
	 * Adds a {@link ManagedObjectSource} to this {@link OfficeFloorBuilder}.
	 * 
	 * @param <O>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName  Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass Class of the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> ManagedObjectBuilder<F> addManagedObject(
			String managedObjectSourceName, Class<MS> managedObjectSourceClass);

	/**
	 * Adds a {@link ManagedObjectSource} to this {@link OfficeFloorBuilder}.
	 * 
	 * @param <O>                     Dependency key type.
	 * @param <F>                     Flow key type.
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instance to use.
	 * @return {@link ManagedObjectBuilder}.
	 */
	<O extends Enum<O>, F extends Enum<F>> ManagedObjectBuilder<F> addManagedObject(String managedObjectSourceName,
			ManagedObjectSource<O, F> managedObjectSource);

	/**
	 * Adds a {@link Team} which will execute {@link FunctionState} instances within
	 * this {@link OfficeFloor}.
	 * 
	 * @param <TS>            {@link TeamSource} type.
	 * @param teamName        Name to register the {@link Team} under.
	 * @param teamSourceClass {@link TeamSource} {@link Class} to source the
	 *                        {@link Team}.
	 * @return {@link TeamBuilder} to build the {@link Team}.
	 */
	<TS extends TeamSource> TeamBuilder<TS> addTeam(String teamName, Class<TS> teamSourceClass);

	/**
	 * Adds a {@link Team} which will execute {@link FunctionState} instances within
	 * the {@link OfficeFloor}.
	 *
	 * @param <TS>       {@link TeamSource} type.
	 * @param teamName   Name to register the {@link Team} under.
	 * @param teamSource {@link TeamSource} to source the {@link Team}.
	 * @return {@link TeamBuilder} to build the {@link Team}.
	 */
	<TS extends TeamSource> TeamBuilder<TS> addTeam(String teamName, TS teamSource);

	/**
	 * Specifies an {@link Executive} which will manage the {@link Team} instances
	 * within the {@link OfficeFloor}.
	 *
	 * @param <XS>                 {@link ExecutiveSource} type.
	 * @param executiveSourceClass {@link ExecutiveSource} {@link Class} to source
	 *                             the {@link Executive}.
	 * @return {@link ExecutiveBuilder} to build the {@link Executive}.
	 */
	<XS extends ExecutiveSource> ExecutiveBuilder<XS> setExecutive(Class<XS> executiveSourceClass);

	/**
	 * Specifies an {@link Executive} which will manage the {@link Team} instances
	 * within the {@link OfficeFloor}.
	 * 
	 * @param <XS>            {@link ExecutiveSource} type.
	 * @param executiveSource {@link ExecutiveSource} to source the
	 *                        {@link Executive}.
	 * @return {@link ExecutiveBuilder} to build the {@link Executive}.
	 */
	<XS extends ExecutiveSource> ExecutiveBuilder<XS> setExecutive(XS executiveSource);

	/**
	 * Adds an {@link Office} on the {@link OfficeFloor}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return {@link OfficeBuilder} to build the {@link Office}.
	 */
	OfficeBuilder addOffice(String officeName);

	/**
	 * Specifies the {@link EscalationHandler} for issues escalating out of the
	 * {@link Office} instances.
	 * 
	 * @param escalationHandler {@link EscalationHandler}.
	 */
	void setEscalationHandler(EscalationHandler escalationHandler);

	/**
	 * Builds the {@link OfficeFloor}.
	 * 
	 * @param issuesListener {@link OfficeFloorIssues} to listen for issues in
	 *                       constructing the {@link OfficeFloor}.
	 * @return Built {@link OfficeFloor} if successfully built, or <code>null</code>
	 *         if could not construct {@link OfficeFloor} with reasons passed to the
	 *         {@link OfficeFloorIssues}.
	 */
	OfficeFloor buildOfficeFloor(OfficeFloorIssues issuesListener);

	/**
	 * Builds the {@link OfficeFloor}.
	 * 
	 * @return Built {@link OfficeFloor}.
	 * @throws OfficeFloorBuildException If fails to build the {@link OfficeFloor}.
	 * 
	 * @see OfficeFloorBuildException
	 */
	OfficeFloor buildOfficeFloor() throws OfficeFloorBuildException;

}
