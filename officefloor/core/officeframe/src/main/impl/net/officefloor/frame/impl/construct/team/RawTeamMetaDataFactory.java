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
package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.source.OfficeFloorIssueTarget;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.team.ThreadLocalAwareContextImpl;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Factory for the construction of {@link RawTeamMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTeamMetaDataFactory {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * {@link ThreadFactoryManufacturer}.
	 */
	private final ThreadFactoryManufacturer threadFactoryManufacturer;

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext             {@link SourceContext}.
	 * @param executive                 {@link Executive}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 * @param threadLocalAwareExecutor  {@link ThreadLocalAwareExecutor}.
	 */
	public RawTeamMetaDataFactory(SourceContext sourceContext, Executive executive,
			ThreadFactoryManufacturer threadFactoryManufacturer, ThreadLocalAwareExecutor threadLocalAwareExecutor) {
		this.sourceContext = sourceContext;
		this.executive = executive;
		this.threadFactoryManufacturer = threadFactoryManufacturer;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
	}

	/**
	 * Constructs the {@link RawTeamMetaData}.
	 * 
	 * @param                 <TS> {@link TeamSource} type.
	 * @param configuration   {@link TeamConfiguration}.
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @param issues          {@link OfficeFloorIssues}.
	 * @return {@link RawTeamMetaData} or <code>null</code> if fails to construct.
	 */
	public <TS extends TeamSource> RawTeamMetaData constructRawTeamMetaData(TeamConfiguration<TS> configuration,
			String officeFloorName, OfficeFloorIssues issues) {

		// Obtain the team name
		String teamName = configuration.getTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName, "Team added without a name");
			return null; // can not carry on
		}

		// Obtain the team size
		int teamSize = configuration.getTeamSize();
		if (teamSize < 0) {
			issues.addIssue(AssetType.TEAM, teamName, "Team size can not be negative");
			return null; // can not carry on
		}

		// Obtain the team source
		TS teamSource = configuration.getTeamSource();
		if (teamSource == null) {
			Class<TS> teamSourceClass = configuration.getTeamSourceClass();
			if (teamSourceClass == null) {
				issues.addIssue(AssetType.TEAM, teamName, "No TeamSource class provided");
				return null; // can not carry on
			}

			// Instantiate the team source
			teamSource = ConstructUtil.newInstance(teamSourceClass, TeamSource.class, "Team Source '" + teamName + "'",
					AssetType.TEAM, teamName, issues);
			if (teamSource == null) {
				return null; // can not carry on
			}
		}

		Team team;
		boolean isRequireThreadLocalAwareness = false;
		try {

			// Create the executive context
			SourceProperties properties = configuration.getProperties();
			ExecutiveContext executiveContext = new ExecutiveContextImpl(false, teamName, teamSize, teamSource,
					this.threadFactoryManufacturer, properties, this.sourceContext);

			// Create the team
			team = this.executive.createTeam(executiveContext);
			if (team == null) {
				// Indicate failed to provide team
				issues.addIssue(AssetType.TEAM, teamName,
						TeamSource.class.getSimpleName() + " failed to provide " + Team.class.getSimpleName());
				return null; // can not carry on
			}

			// Determine if requires thread local awareness
			if (team instanceof ThreadLocalAwareTeam) {
				ThreadLocalAwareTeam threadLocalAwareTeam = (ThreadLocalAwareTeam) team;
				if (threadLocalAwareTeam.isThreadLocalAware()) {
					threadLocalAwareTeam
							.setThreadLocalAwareness(new ThreadLocalAwareContextImpl(this.threadLocalAwareExecutor));
					isRequireThreadLocalAwareness = true;
				}
			}

		} catch (AbstractSourceError ex) {
			ex.addIssue(new OfficeFloorIssueTarget(issues, AssetType.TEAM, teamName));
			return null; // can not carry on

		} catch (Throwable ex) {
			// Indicate failure to initialise
			issues.addIssue(AssetType.TEAM, teamName, "Failed to create Team", ex);
			return null; // can not carry on
		}

		// Create the management for the team
		TeamManagement teamManagement = new TeamManagementImpl(team);

		// Return the raw meta-data
		return new RawTeamMetaData(teamName, teamManagement, isRequireThreadLocalAwareness);
	}

}