/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.officefloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuildException;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.executive.ExecutiveBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.team.TeamBuilderImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.officefloor.ThreadLocalAwareExecutorImpl;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Implementation of {@link OfficeFloorBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorBuilderImpl implements OfficeFloorBuilder, OfficeFloorConfiguration {

	/**
	 * Name of the break {@link FunctionState} chain {@link Team}.
	 */
	private static final String BREAK_CHAIN_TEAM_NAME = "_BREAK_CHAIN_";

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private final String officeFloorName;

	/**
	 * Listing of {@link ManagedObjectSourceConfiguration} instances.
	 */
	private final List<ManagedObjectSourceConfiguration<?, ?>> mangedObjects = new LinkedList<ManagedObjectSourceConfiguration<?, ?>>();

	/**
	 * Listing of {@link TeamConfiguration} instances.
	 */
	private final List<TeamConfiguration<?>> teams = new LinkedList<TeamConfiguration<?>>();

	/**
	 * Maximum time in milliseconds to wait for {@link OfficeFloor} to start.
	 */
	private long maxStartupWaitTime = 10 * 1000;

	/**
	 * {@link ExecutiveConfiguration}.
	 */
	private ExecutiveConfiguration<?> executiveConfiguration = null;

	/**
	 * Break {@link FunctionState} chain {@link Team}. Initiate with default
	 * {@link TeamSource}.
	 */
	private TeamBuilderImpl<?> breakChainTeam = new TeamBuilderImpl<>(BREAK_CHAIN_TEAM_NAME,
			ExecutorCachedTeamSource.class);

	/**
	 * Listing of {@link OfficeConfiguration} instances.
	 */
	private final List<OfficeConfiguration> offices = new LinkedList<OfficeConfiguration>();

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * {@link ClockFactory}.
	 */
	private ClockFactory clockFactory = null;

	/**
	 * Decorator of the {@link Thread} instances created by the
	 * {@link TeamSourceContext}.
	 */
	private Consumer<Thread> threadDecorator = null;

	/**
	 * {@link ResourceSource} instances.
	 */
	private final List<ResourceSource> resourceSources = new LinkedList<ResourceSource>();

	/**
	 * {@link EscalationProcedure}.
	 */
	private EscalationHandler escalationHandler = null;

	/**
	 * {@link OfficeFloorListener} instances.
	 */
	private final List<OfficeFloorListener> listeners = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 */
	public OfficeFloorBuilderImpl(String officeFloorName) {
		this.officeFloorName = officeFloorName;
	}

	/*
	 * ================ OfficeFloorBuilder ================================
	 */

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void setClockFactory(ClockFactory clockFactory) {
		this.clockFactory = clockFactory;
	}

	@Override
	public void setMaxStartupWaitTime(long maxStartupWaitTime) {
		this.maxStartupWaitTime = maxStartupWaitTime;
	}

	@Override
	public void setThreadDecorator(Consumer<Thread> decorator) {
		this.threadDecorator = decorator;
	}

	@Override
	public void addResources(ResourceSource resourceSource) {
		this.resourceSources.add(resourceSource);
	}

	@Override
	public void addOfficeFloorListener(OfficeFloorListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> addManagedObject(
			String managedObjectSourceName, Class<MS> managedObjectSourceClass) {
		// Create, register and return the builder
		ManagedObjectBuilderImpl<D, F, MS> builder = new ManagedObjectBuilderImpl<D, F, MS>(managedObjectSourceName,
				managedObjectSourceClass);
		this.mangedObjects.add(builder);
		return builder;
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> ManagedObjectBuilder<F> addManagedObject(
			String managedObjectSourceName, ManagedObjectSource<D, F> managedObjectSource) {
		// Create, register and return the builder
		ManagedObjectBuilderImpl<D, F, ManagedObjectSource<D, F>> builder = new ManagedObjectBuilderImpl<D, F, ManagedObjectSource<D, F>>(
				managedObjectSourceName, managedObjectSource);
		this.mangedObjects.add(builder);
		return builder;
	}

	@Override
	public <TS extends TeamSource> TeamBuilder<TS> addTeam(String teamName, Class<TS> teamSourceClass) {
		// Create, register and return the builder
		TeamBuilderImpl<TS> builder = new TeamBuilderImpl<TS>(teamName, teamSourceClass);
		this.teams.add(builder);
		return builder;
	}

	@Override
	public <TS extends TeamSource> TeamBuilder<TS> addTeam(String teamName, TS teamSource) {
		// Create, register and return the builder
		TeamBuilderImpl<TS> builder = new TeamBuilderImpl<>(teamName, teamSource);
		this.teams.add(builder);
		return builder;
	}

	@Override
	public <XS extends ExecutiveSource> ExecutiveBuilder<XS> setExecutive(Class<XS> executiveSourceClass) {
		ExecutiveBuilderImpl<XS> builder = new ExecutiveBuilderImpl<>(executiveSourceClass);
		this.executiveConfiguration = builder;
		return builder;
	}

	@Override
	public <XS extends ExecutiveSource> ExecutiveBuilder<XS> setExecutive(XS executiveSource) {
		ExecutiveBuilderImpl<XS> builder = new ExecutiveBuilderImpl<>(executiveSource);
		this.executiveConfiguration = builder;
		return builder;
	}

	@Override
	public TeamConfiguration<?> getBreakChainTeamConfiguration() {
		return this.breakChainTeam;
	}

	@Override
	public OfficeBuilder addOffice(String officeName) {
		// Create, register and return the builder
		OfficeBuilderImpl builder = new OfficeBuilderImpl(officeName);
		this.offices.add(builder);
		return builder;
	}

	@Override
	public void setEscalationHandler(EscalationHandler escalationHandler) {
		this.escalationHandler = escalationHandler;
	}

	@Override
	public OfficeFloor buildOfficeFloor() throws OfficeFloorBuildException {
		return OfficeFloorBuildException.buildOfficeFloor(this);
	}

	@Override
	public OfficeFloor buildOfficeFloor(OfficeFloorIssues issues) {

		// Build this OfficeFloor
		ThreadLocalAwareExecutor threadLocalAwareExecutor = new ThreadLocalAwareExecutorImpl();
		RawOfficeFloorMetaData rawMetaData = new RawOfficeFloorMetaDataFactory(threadLocalAwareExecutor)
				.constructRawOfficeFloorMetaData(this, issues);
		if (rawMetaData == null) {
			return null; // failed to construct
		}

		// Create the listing of OfficeFloor listeners
		List<OfficeFloorListener> listeners = new ArrayList<>(this.listeners);
		listeners.addAll(Arrays.asList(rawMetaData.getOfficeFloorListeners()));

		// Obtain the OfficeFloor meta-data and return the OfficeFloor
		OfficeFloorMetaData metaData = rawMetaData.getOfficeFloorMetaData();
		return new OfficeFloorImpl(metaData, listeners.toArray(new OfficeFloorListener[listeners.size()]));
	}

	/*
	 * ================== OfficeFloorConfiguration ========================
	 */

	@Override
	public String getOfficeFloorName() {
		return this.officeFloorName;
	}

	@Override
	public long getMaxStartupWaitTime() {
		return this.maxStartupWaitTime;
	}

	@Override
	public Consumer<Thread> getThreadDecorator() {
		return this.threadDecorator;
	}

	@Override
	public SourceContext getSourceContext(Supplier<ClockFactory> clockFactoryProvider) {

		// Obtain the class loader
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Obtain the clock factory
		ClockFactory clockFactory = this.clockFactory;
		if (clockFactory == null) {
			clockFactory = clockFactoryProvider.get();
		}

		// Create and return the source context
		return new SourceContextImpl(false, classLoader, clockFactory,
				this.resourceSources.toArray(new ResourceSource[this.resourceSources.size()]));
	}

	@Override
	public ManagedObjectSourceConfiguration<?, ?>[] getManagedObjectSourceConfiguration() {
		return this.mangedObjects.toArray(new ManagedObjectSourceConfiguration[0]);
	}

	@Override
	public TeamConfiguration<?>[] getTeamConfiguration() {
		return this.teams.toArray(new TeamConfiguration[0]);
	}

	@Override
	public ExecutiveConfiguration<?> getExecutiveConfiguration() {
		return this.executiveConfiguration;
	}

	@Override
	public <TS extends TeamSource> TeamBuilder<TS> setBreakChainTeam(Class<TS> teamSourceClass) {
		TeamBuilderImpl<TS> builder = new TeamBuilderImpl<>(BREAK_CHAIN_TEAM_NAME, teamSourceClass);
		this.breakChainTeam = builder;
		return builder;
	}

	@Override
	public OfficeConfiguration[] getOfficeConfiguration() {
		return this.offices.toArray(new OfficeConfiguration[0]);
	}

	@Override
	public EscalationHandler getEscalationHandler() {
		return this.escalationHandler;
	}

}