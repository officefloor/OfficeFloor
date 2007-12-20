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
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.officefloor.OfficeFloorLoader;
import net.officefloor.repository.ConfigurationItem;

/**
 * {@link net.officefloor.model.officefloor.OfficeFloorModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class OfficeFloorEntry extends
		AbstractEntry<OfficeFloorBuilder, OfficeFloorModel> {

	/**
	 * Loads the {@link OfficeFloorEntry}.
	 * 
	 * @param configuration
	 *            Configuration of the {@link OfficeFloorModel}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link OfficeFloorEntry}.
	 */
	public static OfficeFloorEntry loadOfficeFloor(
			ConfigurationItem configuration, OfficeFloorCompilerContext context)
			throws Exception {

		// Create the Office Floor Model
		OfficeFloorModel officeFloorModel = new OfficeFloorLoader(context
				.getModelRepository()).loadOfficeFloor(configuration);

		// Create the Office Floor entry
		OfficeFloorEntry officeFloorEntry = new OfficeFloorEntry(configuration
				.getId(), context.getBuilderFactory()
				.createOfficeFloorBuilder(), officeFloorModel);

		// Load the Managed Object Source instances
		for (ManagedObjectSourceModel mosModel : officeFloorModel
				.getManagedObjectSources()) {
			ManagedObjectSourceEntry.loadManagedObjectSource(mosModel,
					officeFloorEntry, context);
		}

		// Build the Team instances
		for (TeamModel teamModel : officeFloorModel.getTeams()) {
			TeamEntry.loadTeam(teamModel, officeFloorEntry, context);
		}

		// Build the Offices
		for (OfficeFloorOfficeModel office : officeFloorModel.getOffices()) {
			OfficeEntry.loadOffice(office.getName(), context
					.getConfigurationContext().getConfigurationItem(
							office.getId()), officeFloorEntry, context);
		}

		// Return the Office Floor entry
		return officeFloorEntry;
	}

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the {@link OfficeFloorModel}.
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 * @param model
	 *            {@link OfficeFloorModel}.
	 */
	public OfficeFloorEntry(String id, OfficeFloorBuilder builder,
			OfficeFloorModel model) {
		super(id, builder, model);
	}

	/**
	 * Obtains the {@link OfficeFloorOfficeModel} by the input Id.
	 * 
	 * @param id
	 *            Id of the {@link OfficeFloorOfficeModel}.
	 * @return {@link OfficeFloorOfficeModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public OfficeFloorOfficeModel getOfficeFloorOfficeModel(String id)
			throws Exception {

		// Obtain the Office
		if (id != null) {
			for (OfficeFloorOfficeModel office : this.getModel().getOffices()) {
				if (id.equals(office.getName())) {
					return office;
				}
			}
		}

		// Not exist if here
		throw new Exception("Unknown office '" + id + "' for the office floor "
				+ this.getId());
	}

	/**
	 * Obtains the {@link OfficeTeamModel}.
	 * 
	 * @param officeId
	 *            Id of the {@link net.officefloor.frame.api.manage.Office}.
	 * @param teamName
	 *            Name of the {@link net.officefloor.frame.spi.team.Team}.
	 * @return {@link OfficeTeamModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public OfficeTeamModel getOfficeTeamModel(String officeId, String teamName)
			throws Exception {

		// Obtain the Office
		OfficeFloorOfficeModel office = this
				.getOfficeFloorOfficeModel(officeId);

		// Obtain the Team from the Office
		for (OfficeTeamModel team : office.getTeams()) {
			if (teamName.equals(team.getTeamName())) {
				return team;
			}
		}

		// Not exist if here
		throw new Exception("Unknown team '" + teamName + "' for office '"
				+ officeId + "'");
	}
}
