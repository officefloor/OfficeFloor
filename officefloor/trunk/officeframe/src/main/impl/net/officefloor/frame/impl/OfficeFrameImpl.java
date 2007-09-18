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
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.issue.OfficeIssuesListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.BuilderFactoryImpl;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.spi.team.Team;

/**
 * Default implementation of the {@link net.officefloor.frame.api.OfficeFrame}.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.OfficeFloor#getMetaDataFactory()
	 */
	public BuilderFactory getMetaDataFactory() {
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

		// Create the Offices
		Map<String, RawOfficeMetaData> rawOffices = new HashMap<String, RawOfficeMetaData>();
		Map<String, OfficeImpl> offices = new HashMap<String, OfficeImpl>();
		for (OfficeConfiguration officeConfig : officeFloorConfig
				.getOfficeConfiguration()) {
			// Obtain the Office name
			String officeName = officeConfig.getOfficeName();

			// Create the office
			RawOfficeMetaData rawOfficeMetaData = RawOfficeMetaData
					.createOffice(officeConfig, teamRegistry, rawMosRegistry,
							rawAssetRegistry);

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

}
