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
package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.team.ThreadLocalAwareContextImpl;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawTeamMetaDataFactory;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Raw {@link Team} meta-data implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTeamMetaDataImpl implements RawTeamMetaDataFactory, RawTeamMetaData {

	/**
	 * Obtains the {@link RawTeamMetaDataFactory}.
	 * 
	 * @return {@link RawTeamMetaDataFactory}.
	 */
	public static RawTeamMetaDataFactory getFactory() {
		return new RawTeamMetaDataImpl(null, null, false);
	}

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link TeamManagement}.
	 */
	private final TeamManagement team;

	/**
	 * Flag indicating if a {@link ThreadLocalAwareTeam}.
	 */
	private final boolean isRequireThreadLocalAwareness;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of {@link Team}.
	 * @param team
	 *            {@link TeamManagement}.
	 * @param isRequireThreadLocalAwareness
	 *            Flag indicating if a {@link ThreadLocalAwareTeam}.
	 */
	private RawTeamMetaDataImpl(String teamName, TeamManagement team, boolean isRequireThreadLocalAwareness) {
		this.teamName = teamName;
		this.team = team;
		this.isRequireThreadLocalAwareness = isRequireThreadLocalAwareness;
	}

	/*
	 * =============== RawTeamMetaDataFactory =============================
	 */

	@Override
	public <TS extends TeamSource> RawTeamMetaData constructRawTeamMetaData(TeamConfiguration<TS> configuration,
			SourceContext sourceContext, ThreadLocalAwareExecutor threadLocalAwareExecutor, OfficeFloorIssues issues) {

		// Obtain the team name
		String teamName = configuration.getTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class.getSimpleName(), "Team added without a name");
			return null; // can not carry on
		}

		// Obtain the team source
		Class<TS> teamSourceClass = configuration.getTeamSourceClass();
		if (teamSourceClass == null) {
			issues.addIssue(AssetType.TEAM, teamName, "No TeamSource class provided");
			return null; // can not carry on
		}

		// Instantiate the team source
		TeamSource teamSource = ConstructUtil.newInstance(teamSourceClass, TeamSource.class,
				"Team Source '" + teamName + "'", AssetType.TEAM, teamName, issues);
		if (teamSource == null) {
			return null; // can not carry one
		}

		Team team;
		boolean isRequireThreadLocalAwareness = false;
		try {
			// Create the team source context
			SourceProperties properties = configuration.getProperties();
			TeamSourceContextImpl context = new TeamSourceContextImpl(false, teamName, properties, sourceContext);

			// Create the team
			team = teamSource.createTeam(context);
			if (team == null) {
				// Indicate failed to provide team
				issues.addIssue(AssetType.TEAM, teamName, "TeamSource failed to provide Team");
				return null; // can not carry on
			}

			// Determine if requires thread local awareness
			if (team instanceof ThreadLocalAwareTeam) {
				ThreadLocalAwareTeam threadLocalAwareTeam = (ThreadLocalAwareTeam) team;
				threadLocalAwareTeam.setThreadLocalAwareness(new ThreadLocalAwareContextImpl(threadLocalAwareExecutor));
				isRequireThreadLocalAwareness = true;
			}

		} catch (UnknownPropertyError ex) {
			// Indicate an unknown property
			issues.addIssue(AssetType.TEAM, teamName, "Must specify property '" + ex.getUnknownPropertyName() + "'");
			return null; // can not carry on

		} catch (UnknownClassError ex) {
			// Indicate an unknown class
			issues.addIssue(AssetType.TEAM, teamName, "Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // can not carry on

		} catch (UnknownResourceError ex) {
			// Indicate an unknown resource
			issues.addIssue(AssetType.TEAM, teamName,
					"Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "'");
			return null; // can not carry on

		} catch (Throwable ex) {
			// Indicate failure to initialise
			issues.addIssue(AssetType.TEAM, teamName, "Failed to create Team", ex);
			return null; // can not carry on
		}

		// Create the management for the team
		TeamManagement teamManagement = new TeamManagementImpl(team);

		// Return the raw meta-data
		return new RawTeamMetaDataImpl(teamName, teamManagement, isRequireThreadLocalAwareness);
	}

	/*
	 * =============== RawTeamMetaData =============================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public TeamManagement getTeamManagement() {
		return this.team;
	}

	@Override
	public boolean isRequireThreadLocalAwareness() {
		return this.isRequireThreadLocalAwareness;
	}

}