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
import net.officefloor.frame.api.build.OfficeVisitor;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.executive.ExecutiveBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.team.TeamBuilderImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.officefloor.ThreadLocalAwareExecutorImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Implementation of {@link OfficeFloorBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorBuilderImpl implements OfficeFloorBuilder, OfficeFloorConfiguration {

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
	 * Listing of {@link OfficeConfiguration} instances.
	 */
	private final List<OfficeConfiguration> offices = new LinkedList<OfficeConfiguration>();

	/**
	 * Listing of profiles.
	 */
	private final List<String> profiles = new LinkedList<>();

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
	private final List<ResourceSource> resourceSources = new LinkedList<>();

	/**
	 * {@link OfficeVisitor} instances.
	 */
	private final List<OfficeVisitor> officeVisitors = new LinkedList<>();

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
	public void addProfile(String profile) {
		this.profiles.add(profile);
	}

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
	public OfficeBuilder addOffice(String officeName) {
		// Create, register and return the builder
		OfficeBuilderImpl builder = new OfficeBuilderImpl(officeName);
		this.offices.add(builder);
		return builder;
	}

	@Override
	public void addOfficeVisitor(OfficeVisitor visitor) {
		this.officeVisitors.add(visitor);
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
		return new OfficeFloorImpl(metaData, listeners.toArray(new OfficeFloorListener[listeners.size()]),
				rawMetaData.getExecutive(), rawMetaData.getBackgroundSchedulings(), rawMetaData.getStartupNotify());
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
	public String[] getProfiles() {
		return this.profiles.toArray(new String[this.profiles.size()]);
	}

	@Override
	public Consumer<Thread> getThreadDecorator() {
		return this.threadDecorator;
	}

	@Override
	public SourceContext getSourceContext(String sourceName, Supplier<ClockFactory> clockFactoryProvider) {

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
		return new SourceContextImpl(sourceName, false, this.profiles.toArray(new String[this.profiles.size()]),
				classLoader, clockFactory,
				this.resourceSources.toArray(new ResourceSource[this.resourceSources.size()]));
	}

	@Override
	public ManagedObjectSourceConfiguration<?, ?>[] getManagedObjectSourceConfiguration() {
		return this.mangedObjects.toArray(new ManagedObjectSourceConfiguration[this.mangedObjects.size()]);
	}

	@Override
	public TeamConfiguration<?>[] getTeamConfiguration() {
		return this.teams.toArray(new TeamConfiguration[this.teams.size()]);
	}

	@Override
	public ExecutiveConfiguration<?> getExecutiveConfiguration() {
		return this.executiveConfiguration;
	}

	@Override
	public OfficeConfiguration[] getOfficeConfiguration() {
		return this.offices.toArray(new OfficeConfiguration[this.offices.size()]);
	}

	@Override
	public OfficeVisitor[] getOfficeVisitors() {
		return this.officeVisitors.toArray(new OfficeVisitor[this.officeVisitors.size()]);
	}

	@Override
	public EscalationHandler getEscalationHandler() {
		return this.escalationHandler;
	}

}
