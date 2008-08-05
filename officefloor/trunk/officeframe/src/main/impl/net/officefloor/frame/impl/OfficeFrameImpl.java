/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.issue.OfficeIssuesListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.BuilderFactoryImpl;
import net.officefloor.frame.impl.execute.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.team.Team;

/**
 * Default implementation of the {@link OfficeFrame}.
 * 
 * @author Daniel
 */
public class OfficeFrameImpl extends OfficeFrame {

	/**
	 * {@link BuilderFactory}.
	 */
	protected final BuilderFactory metaDataFactory = new BuilderFactoryImpl();

	/**
	 * Registry of {@link OfficeFloor} instances by their name.
	 */
	protected final Map<String, OfficeFloor> officeFloors = new HashMap<String, OfficeFloor>();

	/**
	 * Clears the {@link OfficeFloor} instances.
	 */
	public synchronized void clearOfficeFloors() {
		this.officeFloors.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.OfficeFloor#getMetaDataFactory()
	 */
	public BuilderFactory getBuilderFactory() {
		return this.metaDataFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.OfficeFrame#registerOfficeFloor(java.lang.String,
	 *      net.officefloor.frame.api.build.OfficeFloorBuilder,
	 *      net.officefloor.frame.api.build.issue.OfficeIssuesListener)
	 */
	protected OfficeFloor registerOfficeFloor(String name,
			OfficeFloorBuilder officeFloorBuilder,
			OfficeIssuesListener issuesListener) throws Exception {

		// Check if Office Floor already registered
		if (this.officeFloors.get(name) != null) {
			throw new IllegalStateException(
					"Office Floor already registered under name '" + name + "'");
		}

		// Create and register the office floor
		OfficeFloor officeFloor = this.createOfficeFloor(officeFloorBuilder);
		this.officeFloors.put(name, officeFloor);

		// Return the Office Floor
		return officeFloor;
	}

	/**
	 * Creates the {@link OfficeFloor} for the input {@link OfficeFloorBuilder}.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails.
	 */
	protected OfficeFloor createOfficeFloor(
			OfficeFloorBuilder officeFloorBuilder) throws Exception {

		// Transform Office Floor Builder into Configuration
		OfficeFloorConfiguration officeFloorConfig = (OfficeFloorConfiguration) officeFloorBuilder;

		// Create the Asset Manager registry
		RawAssetManagerRegistry rawAssetRegistry = new RawAssetManagerRegistry();

		// Create the registry of raw Managed Object meta-data
		RawManagedObjectRegistry rawMosRegistry = RawManagedObjectRegistry
				.createRawManagedObjectMetaDataRegistry(officeFloorConfig,
						rawAssetRegistry, this);

		// Obtain the registry of teams
		Map<String, Team> teamRegistry = officeFloorConfig.getTeamRegistry();

		// Obtain the office floor escalation procedure
		EscalationProcedure officeFloorEscalationProcedure = officeFloorConfig
				.getEscalationProcedure();
		if (officeFloorEscalationProcedure == null) {
			officeFloorEscalationProcedure = new EscalationProcedureImpl();
		}

		// Create the Offices
		Map<String, RawOfficeMetaData> rawOffices = new HashMap<String, RawOfficeMetaData>();
		Map<String, OfficeImpl> offices = new HashMap<String, OfficeImpl>();
		for (OfficeConfiguration officeConfig : officeFloorConfig
				.getOfficeConfiguration()) {
			// Obtain the Office name
			String officeName = officeConfig.getOfficeName();

			// Enhance the office
			OfficeEnhancerContext flowNodesEnhancerContext = new OfficeEnhancerContextImpl(
					officeConfig, officeFloorConfig);
			for (OfficeEnhancer officeEnhancer : officeConfig
					.getOfficeEnhancers()) {
				officeEnhancer.enhanceOffice(flowNodesEnhancerContext);
			}

			// Create the office
			RawOfficeMetaData rawOfficeMetaData = RawOfficeMetaData
					.createOffice(officeConfig, teamRegistry, rawMosRegistry,
							rawAssetRegistry, officeFloorEscalationProcedure);

			// Register the office
			rawOffices.put(officeName, rawOfficeMetaData);
			offices.put(officeName, rawOfficeMetaData.getOffice());
		}

		// Link the Managed Objects with Tasks
		rawMosRegistry.loadRemainingManagedObjectState(rawOffices);

		// Create the set of teams
		Set<Team> teams = new HashSet<Team>();
		for (Team team : teamRegistry.values()) {
			teams.add(team);
		}

		// Return the Office Floor
		return new OfficeFloorImpl(teams, offices);
	}

	/**
	 * {@link OfficeEnhancerContext} implementation.
	 */
	private class OfficeEnhancerContextImpl implements OfficeEnhancerContext {

		/**
		 * {@link OfficeConfiguration}.
		 */
		private final OfficeConfiguration officeConfig;

		/**
		 * {@link OfficeFloorConfiguration}.
		 */
		private final OfficeFloorConfiguration officeFloorConfig;

		/**
		 * Initiate.
		 * 
		 * @param officeConfig
		 *            {@link OfficeConfiguration}.
		 * @param officeFloorConfig
		 *            {@link OfficeFloorConfiguration}.
		 */
		public OfficeEnhancerContextImpl(OfficeConfiguration officeConfig,
				OfficeFloorConfiguration officeFloorConfig) {
			this.officeConfig = officeConfig;
			this.officeFloorConfig = officeFloorConfig;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodesEnhancerContext#getFlowNodeBuilder(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public FlowNodeBuilder<?> getFlowNodeBuilder(String workName,
				String taskName) throws BuildException {
			return this.getFlowNodeBuilder(null, workName, taskName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodesEnhancerContext#getFlowNodeBuilder(java.lang.String,
		 *      java.lang.String, java.lang.String)
		 */
		@Override
		public FlowNodeBuilder<?> getFlowNodeBuilder(String namespace,
				String workName, String taskName) throws BuildException {
			try {
				// Obtain the flow node builder
				return this.officeConfig.getFlowNodeBuilder(namespace,
						workName, taskName);
			} catch (ConfigurationException ex) {
				// Propagate
				throw new BuildException(ex.getMessage());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.OfficeEnhancerContext#getManagedObjectHandlerBuilder(java.lang.String,
		 *      java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <H extends Enum<H>> ManagedObjectHandlerBuilder<H> getManagedObjectHandlerBuilder(
				String managedObjectId, Class<H> handlerKeys)
				throws BuildException {
			try {

				// Obtain the managed object source
				ManagedObjectSourceConfiguration<H, ?> mosConfig = null;
				for (ManagedObjectSourceConfiguration<?, ?> mos : this.officeFloorConfig
						.getManagedObjectSourceConfiguration()) {
					if (managedObjectId.equals(mos.getManagedObjectName())) {
						mosConfig = (ManagedObjectSourceConfiguration<H, ?>) mos;
					}
				}
				if (mosConfig == null) {
					throw new BuildException(
							"Can not find managed object source by id '"
									+ managedObjectId + "'");
				}

				// Obtain the managed object handler builder
				ManagedObjectHandlerBuilder<H> handlerBuilder = mosConfig
						.getHandlerBuilder(handlerKeys);

				// Return the managed object handler builder
				return handlerBuilder;

			} catch (ConfigurationException ex) {
				throw new BuildException(ex.getMessage());
			}
		}

	}
}
