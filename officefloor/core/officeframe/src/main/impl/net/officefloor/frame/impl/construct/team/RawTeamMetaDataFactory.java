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

package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
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
	 * {@link TeamOversight}.
	 */
	private final TeamOversight teamOversight;

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
	 * @param teamOversight             {@link TeamOversight}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 * @param threadLocalAwareExecutor  {@link ThreadLocalAwareExecutor}.
	 */
	public RawTeamMetaDataFactory(SourceContext sourceContext, Executive executive, TeamOversight teamOversight,
			ThreadFactoryManufacturer threadFactoryManufacturer, ThreadLocalAwareExecutor threadLocalAwareExecutor) {
		this.sourceContext = sourceContext;
		this.executive = executive;
		this.teamOversight = teamOversight;
		this.threadFactoryManufacturer = threadFactoryManufacturer;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
	}

	/**
	 * Constructs the {@link RawTeamMetaData}.
	 * 
	 * @param <TS>            {@link TeamSource} type.
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

		// Determine if request no team oversight
		boolean isRequestNoTeamOversite = configuration.isRequestNoTeamOversight();

		// Construct the team
		Team team;
		boolean isRequireThreadLocalAwareness = false;
		try {

			// Create the executive context
			SourceProperties properties = configuration.getProperties();
			ExecutiveContextImpl executiveContext = new ExecutiveContextImpl(false, teamName, isRequestNoTeamOversite,
					teamSize, teamSource, this.executive, this.threadFactoryManufacturer, properties,
					this.sourceContext);

			// Create the team (via oversight if provided, otherwise directly)
			if (teamOversight != null) {
				team = teamOversight.createTeam(executiveContext);
			} else {
				team = teamSource.createTeam(executiveContext);
			}
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
