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
package net.officefloor.frame.impl.construct.officefloor;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuildException;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataImpl;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaDataImpl;
import net.officefloor.frame.impl.construct.managedfunction.RawManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaDataImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.team.RawTeamMetaDataImpl;
import net.officefloor.frame.impl.construct.team.TeamBuilderImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.officefloor.ThreadLocalAwareExecutorImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;

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
	 * Listing of {@link OfficeConfiguration} instances.
	 */
	private final List<OfficeConfiguration> offices = new LinkedList<OfficeConfiguration>();

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * {@link ResourceSource} instances.
	 */
	private final List<ResourceSource> resourceSources = new LinkedList<ResourceSource>();

	/**
	 * {@link EscalationProcedure}.
	 */
	private EscalationHandler escalationHandler = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorName
	 *            Name of the {@link OfficeFloor}.
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
	public void addResources(ResourceSource resourceSource) {
		this.resourceSources.add(resourceSource);
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
	public OfficeFloor buildOfficeFloor(OfficeFloorIssues issuesListener) {

		// Build this OfficeFloor
		RawOfficeFloorMetaData rawMetaData = RawOfficeFloorMetaDataImpl.getFactory().constructRawOfficeFloorMetaData(
				this, issuesListener, RawTeamMetaDataImpl.getFactory(), new ThreadLocalAwareExecutorImpl(),
				RawManagedObjectMetaDataImpl.getFactory(), RawBoundManagedObjectMetaDataImpl.getFactory(),
				RawGovernanceMetaDataImpl.getFactory(), RawAdministrationMetaDataImpl.getFactory(),
				RawOfficeMetaDataImpl.getFactory(), RawManagedFunctionMetaDataImpl.getFactory());

		// Obtain the office floor meta-data and return the office floor
		OfficeFloorMetaData metaData = rawMetaData.getOfficeFloorMetaData();
		return new OfficeFloorImpl(metaData);
	}

	/*
	 * ================== OfficeFloorConfiguration ========================
	 */

	@Override
	public String getOfficeFloorName() {
		return this.officeFloorName;
	}

	@Override
	public SourceContext getSourceContext() {

		// Obtain the class loader
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Create and return the source context
		return new SourceContextImpl(false, classLoader,
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
	public OfficeConfiguration[] getOfficeConfiguration() {
		return this.offices.toArray(new OfficeConfiguration[0]);
	}

	@Override
	public EscalationHandler getEscalationHandler() {
		return this.escalationHandler;
	}

}