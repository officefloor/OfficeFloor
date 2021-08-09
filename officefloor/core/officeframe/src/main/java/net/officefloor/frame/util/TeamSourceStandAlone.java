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

package net.officefloor.frame.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.construct.team.ExecutiveContextImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.test.MockClockFactory;

/**
 * Loads a {@link TeamSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceStandAlone {

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link SourceProperties} to initialise the {@link TeamSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link Team} size.
	 */
	private int teamSize = 1;

	/**
	 * {@link Thread} decorator. May be <code>null</code>.
	 */
	private Consumer<Thread> threadDecorator = null;

	/**
	 * {@link ThreadCompletionListener} instances.
	 */
	private final List<ThreadCompletionListener> threadCompletionListeners = new LinkedList<>();

	/**
	 * {@link ClockFactory}.
	 */
	private ClockFactory clockFactory = new MockClockFactory();

	/**
	 * Default instantiation.
	 */
	public TeamSourceStandAlone() {
		this.teamName = null;
	}

	/**
	 * Instantiate with {@link Team} name.
	 * 
	 * @param teamName Name of the {@link Team}.
	 */
	public TeamSourceStandAlone(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * Initialises and returns the {@link TeamSource} instance.
	 * 
	 * @param <TS>            {@link TeamSource} type.
	 * @param teamSourceClass {@link Class} of the {@link TeamSource}.
	 * @return Initialised {@link TeamSource}.
	 * @throws Exception If fails instantiation and initialising the
	 *                   {@link TeamSource}.
	 */
	public <TS extends TeamSource> TS loadTeamSource(Class<TS> teamSourceClass) throws Exception {

		// Create the team source
		TS teamSource = teamSourceClass.getDeclaredConstructor().newInstance();

		// Return the team source
		return teamSource;
	}

	/**
	 * Specifies the {@link Team} size.
	 * 
	 * @param teamSize {@link Team} size.
	 */
	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	/**
	 * Adds a property for initialising the {@link Team}.
	 * 
	 * @param name  Name of property.
	 * @param value Value of property.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/**
	 * Specifies the decorator of the {@link Thread} instances created by the
	 * {@link TeamSourceContext}.
	 * 
	 * @param decorator {@link Thread} decorator.
	 */
	public void setThreadDecorator(Consumer<Thread> decorator) {
		this.threadDecorator = decorator;
	}

	/**
	 * Adds a {@link ThreadCompletionListener}.
	 * 
	 * @param threadCompletionListener {@link ThreadCompletionListener}.
	 */
	public void addThreadCompletionListener(ThreadCompletionListener threadCompletionListener) {
		this.threadCompletionListeners.add(threadCompletionListener);
	}

	/**
	 * Specifies the {@link ClockFactory}.
	 * 
	 * @param clockFactory {@link ClockFactory}.
	 */
	public void setClockFactory(ClockFactory clockFactory) {
		this.clockFactory = clockFactory;
	}

	/**
	 * Returns a {@link Team} from the loaded {@link TeamSource}.
	 * 
	 * @param <TS>            {@link TeamSource} type.
	 * @param teamSourceClass {@link Class} of the {@link TeamSource}.
	 * @return {@link Team} from the loaded {@link TeamSource}.
	 * @throws Exception If fails loading the {@link TeamSource} and creating a
	 *                   {@link Team}.
	 */
	public <TS extends TeamSource> Team loadTeam(Class<TS> teamSourceClass) throws Exception {

		// Load the team source
		TS teamSource = this.loadTeamSource(teamSourceClass);

		// Obtain the team name (default to class name if not provided)
		String teamName = (this.teamName != null ? this.teamName : teamSourceClass.getSimpleName());

		// Create team source context
		SourceContext sourceContext = new SourceContextImpl(this.getClass().getName(), false, new String[0],
				Thread.currentThread().getContextClassLoader(), this.clockFactory);
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(
				this.threadCompletionListeners.toArray(new ThreadCompletionListener[0]));
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(managedExecutionFactory,
				this.threadDecorator);
		Executive executive = new DefaultExecutive(threadFactoryManufacturer);
		TeamSourceContext context = new ExecutiveContextImpl(false, teamName, true, this.teamSize, teamSource,
				executive, threadFactoryManufacturer, this.properties, sourceContext);

		// Return the created team
		return teamSource.createTeam(context);
	}

}
