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
package net.officefloor.compile;

import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TeamModel;

/**
 * {@link Team} entry for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class TeamEntry extends AbstractEntry<OfficeFloorBuilder, TeamModel> {

	/**
	 * Loads the {@link TeamEntry}.
	 * 
	 * @param configuration
	 *            {@link TeamModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link TeamEntry}.
	 */
	public static TeamEntry loadTeam(TeamModel configuration,
			OfficeFloorEntry officeFloorEntry,
			OfficeFloorCompilerContext context) {

		// Create the team entry
		TeamEntry team = new TeamEntry(configuration.getId(), configuration,
				officeFloorEntry);

		// Return the team entry
		return team;
	}

	/**
	 * {@link OfficeFloorEntry} containing this {@link TeamEntry}.
	 */
	private final OfficeFloorEntry officeFloorEntry;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the {@link Team}.
	 * @param model
	 *            {@link TeamModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry}.
	 */
	public TeamEntry(String id, TeamModel model,
			OfficeFloorEntry officeFloorEntry) {
		super(id, officeFloorEntry.getBuilder(), model);
		this.officeFloorEntry = officeFloorEntry;
	}

	/**
	 * Obtains the {@link OfficeFloorEntry}.
	 * 
	 * @return {@link OfficeFloorEntry}.
	 */
	public OfficeFloorEntry getOfficeFloorEntry() {
		return this.officeFloorEntry;
	}

	/**
	 * Builds the {@link Team}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	public void build(LoaderContext builderUtil) throws Exception {

		// Obtain the source for the team
		Class<? extends TeamSource> teamSource = builderUtil.obtainClass(this
				.getModel().getTeamFactory(), TeamSource.class);

		// Build the team
		TeamBuilder<?> teamBuilder = this.getBuilder().addTeam(this.getId(),
				teamSource);
		for (PropertyModel property : this.getModel().getProperties()) {
			teamBuilder.addProperty(property.getName(), property.getValue());
		}
	}
}
