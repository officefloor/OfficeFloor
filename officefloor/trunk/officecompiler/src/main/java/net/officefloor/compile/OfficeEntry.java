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
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.OfficeDeskModel;
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

		// Register the office entry
		context.getOfficeRegistry().put(officeEntry.getId(), officeEntry);

		// Load the Room of the Office
		OfficeRoomModel room = model.getRoom();
		if (room != null) {
			RoomEntry.loadRoom(context.getConfigurationContext()
					.getConfigurationItem(room.getId()), officeEntry, context);
		}

		// Return the office entry
		return officeEntry;
	}

	/**
	 * {@link OfficeFloorEntry}.
	 */
	private final OfficeFloorEntry officeFloorEntry;

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
	 * Obtains {@link OfficeDeskModel} by the input desk id.
	 * 
	 * @param deskId
	 *            Id of the {@link OfficeDeskModel}.
	 * @return {@link OfficeDeskModel}.
	 */
	public OfficeDeskModel getOfficeDeskModel(String deskId) {
		return this.getOfficeDeskModel(deskId, this.getModel().getRoom());
	}

	/**
	 * Obtains the {@link FlowItemModel}.
	 * 
	 * @param deskId
	 *            Id of the {@link OfficeDeskModel} containing the
	 *            {@link FlowItemModel}.
	 * @param flowItemId
	 *            Id of the {@link FlowItemModel}.
	 * @return {@link FlowItemModel}.
	 * @throws Exception
	 *             If not found.
	 */
	public FlowItemModel getFlowItemModel(String deskId, String flowItemId)
			throws Exception {
		// Obtain the desk model
		OfficeDeskModel desk = this.getOfficeDeskModel(deskId, this.getModel()
				.getRoom());

		// Obtain the flow item
		if (desk != null) {
			for (FlowItemModel flowItem : desk.getFlowItems()) {
				if (flowItemId.equals(flowItem.getId())) {
					return flowItem;
				}
			}
		}

		// Not found if here
		throw new Exception("Can not find flow item '" + flowItemId
				+ "' of desk '" + deskId + "'");
	}

	/**
	 * Recursive method to obtain the {@link OfficeDeskModel}.
	 * 
	 * @param deskId
	 *            Id of the {@link OfficeDeskModel}.
	 * @param room
	 *            {@link OfficeRoomModel}.
	 * @return {@link OfficeDeskModel} or <code>null</code> if not found.
	 */
	private OfficeDeskModel getOfficeDeskModel(String deskId,
			OfficeRoomModel room) {

		// Ensure have room
		if (room == null) {
			return null;
		}

		// Search desks of this room
		for (OfficeDeskModel desk : room.getDesks()) {
			if (deskId.equals(desk.getId())) {
				return desk;
			}
		}

		// Search sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {
			OfficeDeskModel desk = this.getOfficeDeskModel(deskId, subRoom);
			if (desk != null) {
				return desk;
			}
		}

		// Not found within this room
		return null;
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
				.getOfficeFloorOfficeModel(this.getId());

		// Register the managed objects
		for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
			String managedObjectId = mo.getManagedObjectSource()
					.getManagedObjectSourceId();
			this.getBuilder().registerManagedObject(mo.getManagedObjectName(),
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
