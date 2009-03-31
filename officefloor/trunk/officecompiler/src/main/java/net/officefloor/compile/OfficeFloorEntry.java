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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.officefloor.OfficeFloorLoader;

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

		// Create the office floor builder
		OfficeFloorBuilder builder = OfficeFrame.getInstance()
				.createOfficeFloorBuilder(configuration.getId());

		// Create the Office Floor entry
		OfficeFloorEntry officeFloorEntry = new OfficeFloorEntry(configuration
				.getId(), builder, officeFloorModel);

		// Load the Managed Object Source instances
		for (ManagedObjectSourceModel mosModel : officeFloorModel
				.getManagedObjectSources()) {
			ManagedObjectSourceEntry mosEntry = ManagedObjectSourceEntry
					.loadManagedObjectSource(mosModel, officeFloorEntry,
							context);
			officeFloorEntry.managedObjectSourceMap.put(mosModel, mosEntry);
		}

		// Load the Team instances
		for (TeamModel teamModel : officeFloorModel.getTeams()) {
			TeamEntry teamEntry = TeamEntry.loadTeam(teamModel,
					officeFloorEntry, context);
			officeFloorEntry.teamMap.put(teamModel, teamEntry);
		}

		// Load the Offices
		for (OfficeFloorOfficeModel officeModel : officeFloorModel.getOffices()) {
			OfficeEntry officeEntry = OfficeEntry.loadOffice(officeModel
					.getName(), context.getConfigurationContext()
					.getConfigurationItem(officeModel.getId()),
					officeFloorEntry, context);
			officeFloorEntry.officeMap.put(officeModel, officeEntry);
		}

		// Return the Office Floor entry
		return officeFloorEntry;
	}

	/**
	 * {@link ManagedObjectSourceModel} to {@link ManagedObjectSourceEntry}
	 * mapping.
	 */
	private final ModelEntryMap<ManagedObjectSourceModel, ManagedObjectSourceEntry> managedObjectSourceMap = new ModelEntryMap<ManagedObjectSourceModel, ManagedObjectSourceEntry>();

	/**
	 * {@link TeamModel} to {@link TeamEntry} mapping.
	 */
	private final ModelEntryMap<TeamModel, TeamEntry> teamMap = new ModelEntryMap<TeamModel, TeamEntry>();

	/**
	 * {@link OfficeFloorOfficeModel} to {@link OfficeEntry} mapping.
	 */
	protected final ModelEntryMap<OfficeFloorOfficeModel, OfficeEntry> officeMap = new ModelEntryMap<OfficeFloorOfficeModel, OfficeEntry>();

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
	 * Obtains the {@link OfficeFloorOfficeModel} for the {@link OfficeEntry}.
	 * 
	 * @param officeEntry
	 *            {@link OfficeEntry}.
	 * @return {@link OfficeFloorOfficeModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public OfficeFloorOfficeModel getOfficeFloorOfficeModel(
			OfficeEntry officeEntry) throws Exception {
		return this.getModel(officeEntry, this.officeMap, "Unknown office '"
				+ officeEntry.getId() + "' for the office floor "
				+ this.getId());
	}

	/**
	 * Obtains the {@link OfficeEntry} for the {@link OfficeFloorOfficeModel}.
	 * 
	 * @param officeModel
	 *            {@link OfficeFloorOfficeModel}.
	 * @return {@link OfficeEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public OfficeEntry getOfficeEntry(OfficeFloorOfficeModel officeModel)
			throws Exception {
		return this.getEntry(officeModel, this.officeMap, "Unknown office '"
				+ officeModel.getName() + "' for the office floor "
				+ this.getId());
	}

	/**
	 * Obtains the {@link OfficeTeamModel}.
	 * 
	 * @param officeEntry
	 *            {@link OfficeEntry}.
	 * @param teamName
	 *            Name of the {@link ExternalTeamModel} on the
	 *            {@link OfficeModel}.
	 * @return {@link OfficeTeamModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public OfficeTeamModel getOfficeTeamModel(OfficeEntry officeEntry,
			String teamName) throws Exception {

		// Obtain the Office
		OfficeFloorOfficeModel office = this
				.getOfficeFloorOfficeModel(officeEntry);

		// Obtain the Team from the Office
		for (OfficeTeamModel team : office.getTeams()) {
			if (teamName.equals(team.getTeamName())) {
				return team;
			}
		}

		// Not exist if here
		throw new Exception("Unknown team '" + teamName + "' for office '"
				+ office.getName() + "' of office floor " + this.getId());
	}

	/**
	 * Obtains the {@link ManagedObjectSourceEntry} for the
	 * {@link ManagedObjectSourceModel}.
	 * 
	 * @param mosModel
	 *            {@link ManagedObjectSourceModel}.
	 * @return {@link ManagedObjectSourceEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public ManagedObjectSourceEntry getManagedObjectSourceEntry(
			ManagedObjectSourceModel mosModel) throws Exception {
		return this.getEntry(mosModel, this.managedObjectSourceMap,
				"No managed object source '" + mosModel.getId()
						+ "' on office floor " + this.getId());
	}

	/**
	 * Obtains the {@link TeamEntry} for the {@link TeamModel}.
	 * 
	 * @param teamModel
	 *            {@link TeamModel}.
	 * @return {@link TeamEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public TeamEntry getTeamEntry(TeamModel teamModel) throws Exception {
		return this.getEntry(teamModel, this.teamMap, "No team '"
				+ teamModel.getId() + "' on office floor " + this.getId());
	}

	/**
	 * Obtains the {@link OfficeEntry} instances of this
	 * {@link OfficeFloorEntry}.
	 * 
	 * @return Listing of all the {@link OfficeEntry} instances of this
	 *         {@link OfficeFloorEntry}.
	 * @throws Exception
	 *             If failure in obtaining the listing.
	 */
	public OfficeEntry[] getOfficeEntries() throws Exception {
		List<OfficeEntry> officeEntries = new LinkedList<OfficeEntry>();
		for (OfficeFloorOfficeModel officeModel : this.getModel().getOffices()) {
			OfficeEntry officeEntry = this.getOfficeEntry(officeModel);
			officeEntries.add(officeEntry);
		}
		return officeEntries.toArray(new OfficeEntry[0]);
	}

	/**
	 * Obtains all the {@link WorkEntry} instances of this
	 * {@link OfficeFloorEntry}.
	 * 
	 * @return Listing of all the {@link WorkEntry} instances of this
	 *         {@link OfficeFloorEntry}.
	 * @throws Exception
	 *             If failure in obtaining the listing.
	 */
	public WorkEntry<?>[] getWorkEntries() throws Exception {
		List<WorkEntry<?>> workEntries = new LinkedList<WorkEntry<?>>();
		for (OfficeEntry officeEntry : this.getOfficeEntries()) {
			OfficeRoomModel officeRoom = officeEntry.getModel().getRoom();
			if (officeRoom != null) {
				RoomEntry roomEntry = officeEntry.getRoomEntry(officeRoom);
				for (DeskEntry deskEntry : roomEntry.getDeskEntries()) {
					for (DeskWorkModel workModel : deskEntry.getModel()
							.getWorks()) {
						WorkEntry<?> workEntry = deskEntry
								.getWorkEntry(workModel);
						workEntries.add(workEntry);
					}
				}
			}
		}
		return workEntries.toArray(new WorkEntry<?>[0]);
	}

	/**
	 * Obtains all the {@link ManagedObjectSourceEntry} instances of this
	 * {@link OfficeFloorEntry}.
	 * 
	 * @return Listing of all the {@link ManagedObjectSourceEntry} instances of
	 *         this {@link OfficeFloorEntry}.
	 * @throws Exception
	 *             If failure in obtaining the listing.
	 */
	public ManagedObjectSourceEntry[] getManagedObjectSourceEntries()
			throws Exception {
		List<ManagedObjectSourceEntry> mosEntries = new LinkedList<ManagedObjectSourceEntry>();
		for (ManagedObjectSourceModel mosModel : this.getModel()
				.getManagedObjectSources()) {
			ManagedObjectSourceEntry mosEntry = this
					.getManagedObjectSourceEntry(mosModel);
			mosEntries.add(mosEntry);
		}
		return mosEntries.toArray(new ManagedObjectSourceEntry[0]);
	}

	/**
	 * Obtains all the {@link TeamEntry} instances of this
	 * {@link OfficeFloorEntry}.
	 * 
	 * @return Listing of all the {@link TeamEntry} instances of this
	 *         {@link OfficeFloorEntry}.
	 * @throws Exception
	 *             If failure in obtaining the listing.
	 */
	public TeamEntry[] getTeamEntries() throws Exception {
		List<TeamEntry> teamEntries = new LinkedList<TeamEntry>();
		for (TeamModel teamModel : this.getModel().getTeams()) {
			TeamEntry teamEntry = this.getTeamEntry(teamModel);
			teamEntries.add(teamEntry);
		}
		return teamEntries.toArray(new TeamEntry[0]);
	}
}
