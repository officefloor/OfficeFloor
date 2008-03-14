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

import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.repository.ConfigurationItem;

/**
 * {@link net.officefloor.model.office.OfficeModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class OfficeEntry extends AbstractEntry<OfficeBuilder, OfficeModel> {

	/**
	 * Loads the {@link OfficeEntry}.
	 * 
	 * @param officeId
	 *            Id of the {@link net.officefloor.frame.api.manage.Office}.
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link OfficeModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry} containing this {@link OfficeEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return {@link OfficeEntry}.
	 */
	public static OfficeEntry loadOffice(String officeId,
			ConfigurationItem configurationItem,
			OfficeFloorEntry officeFloorEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Load the Office
		OfficeModel model = new OfficeLoader(context.getModelRepository())
				.loadOffice(configurationItem);

		// Create the builder
		OfficeBuilder builder = context.getBuilderFactory()
				.createOfficeBuilder();

		// Create the office entry
		OfficeEntry officeEntry = new OfficeEntry(officeId, builder, model,
				officeFloorEntry);

		// Load the Room of the Office
		OfficeRoomModel room = model.getRoom();
		if (room != null) {
			RoomEntry roomEntry = RoomEntry.loadRoom(context
					.getConfigurationContext().getConfigurationItem(
							room.getId()), officeEntry, context);
			officeEntry.roomMap.put(room, roomEntry);
		}

		// Return the office entry
		return officeEntry;
	}

	/**
	 * {@link OfficeFloorEntry}.
	 */
	private final OfficeFloorEntry officeFloorEntry;

	/**
	 * {@link OfficeRoomModel} to {@link RoomEntry} map.
	 */
	private final ModelEntryMap<OfficeRoomModel, RoomEntry> roomMap = new ModelEntryMap<OfficeRoomModel, RoomEntry>();

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the {@link net.officefloor.frame.api.manage.Office}.
	 * @param builder
	 *            {@link OfficeBuilder}.
	 * @param model
	 *            {@link OfficeModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry}.
	 */
	public OfficeEntry(String id, OfficeBuilder builder, OfficeModel model,
			OfficeFloorEntry officeFloorEntry) {
		super(id, builder, model);
		this.officeFloorEntry = officeFloorEntry;
	}

	/**
	 * Obtains the {@link OfficeFloorEntry} containing this {@link OfficeEntry}.
	 * 
	 * @return {@link OfficeFloorEntry} containing this {@link OfficeEntry}.
	 */
	public OfficeFloorEntry getOfficeFloorEntry() {
		return this.officeFloorEntry;
	}

	/**
	 * Obtains the {@link RoomEntry} for the {@link OfficeRoomModel}.
	 * 
	 * @param officeRoom
	 *            {@link OfficeRoomModel}.
	 * @return {@link RoomEntry}.
	 * @throws Exception
	 *             If not found.
	 */
	public RoomEntry getRoomEntry(OfficeRoomModel officeRoom) throws Exception {
		return this.getEntry(officeRoom, this.roomMap, "No room '"
				+ officeRoom.getName() + "' on office " + this.getId());
	}

	/**
	 * Builds the {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	public void build() throws Exception {

		// Obtain the office of the office floor
		OfficeFloorOfficeModel office = this.officeFloorEntry
				.getOfficeFloorOfficeModel(this);

		// Register the managed objects
		for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
			String managedObjectId = mo.getManagedObjectSource()
					.getManagedObjectSourceId();
			String managedObjectName = mo.getManagedObjectName();
			this.getBuilder().registerManagedObject(managedObjectName,
					managedObjectId);
		}

		// Register the teams
		for (OfficeTeamModel team : office.getTeams()) {
			String teamId = team.getTeam().getTeamId();
			this.getBuilder().registerTeam(team.getTeamName(), teamId);
		}

		// Register office with the office floor
		this.getOfficeFloorEntry().getBuilder().addOffice(this.getId(),
				this.getBuilder());
	}

}
