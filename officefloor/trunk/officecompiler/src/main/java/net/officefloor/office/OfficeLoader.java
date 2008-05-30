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
package net.officefloor.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.desk.DeskLoader;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyFlowToFlowItemModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.FlowItemToPostAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToTeamModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.room.RoomLoader;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeLoader {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Default constructor.
	 */
	public OfficeLoader() {
		this.modelRepository = new ModelRepository();
	}

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public OfficeLoader(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * Loads the {@link OfficeModel} from configuration.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link OfficeModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public OfficeModel loadOffice(ConfigurationItem configuration)
			throws Exception {

		// Load the Office Model from configuration
		OfficeModel office = this.modelRepository.retrieve(new OfficeModel(),
				configuration);

		// Create the registry of teams
		Map<String, ExternalTeamModel> teams = new HashMap<String, ExternalTeamModel>();
		for (ExternalTeamModel team : office.getExternalTeams()) {
			teams.put(team.getName(), team);
		}

		// Create the registry of duties
		DoubleKeyMap<String, String, DutyModel> duties = new DoubleKeyMap<String, String, DutyModel>();
		for (AdministratorModel administrator : office.getAdministrators()) {
			for (DutyModel duty : administrator.getDuties()) {
				duties.put(administrator.getId(), duty.getKey(), duty);
			}
		}

		// Link in the Room and its sub rooms
		this.linkInRoom(office.getRoom(), teams, duties);

		// Create the registry of managed objects
		Map<String, ExternalManagedObjectModel> managedObjects = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel managedObject : office
				.getExternalManagedObjects()) {
			managedObjects.put(managedObject.getName(), managedObject);
		}

		// Connect the administrators to their managed objects
		for (AdministratorModel administrator : office.getAdministrators()) {
			for (AdministratorToManagedObjectModel conn : administrator
					.getManagedObjects()) {
				ExternalManagedObjectModel extMo = managedObjects.get(conn
						.getName());
				if (extMo != null) {
					conn.setAdministrator(administrator);
					conn.setManagedObject(extMo);
					conn.connect();
				}
			}
		}

		// Create the registry of flow items
		Map<String, FlowItemModel> flowItems = new HashMap<String, FlowItemModel>();
		this.registerFlowItems(office.getRoom(), flowItems);

		// Connect the duty flows to their flow items
		for (AdministratorModel administrator : office.getAdministrators()) {
			for (DutyModel duty : administrator.getDuties()) {
				for (DutyFlowModel dutyFlow : duty.getFlows()) {
					DutyFlowToFlowItemModel conn = dutyFlow.getFlowItem();
					if (conn != null) {
						FlowItemModel flowItem = flowItems.get(conn
								.getFlowItemId());
						if (flowItem != null) {
							conn.setDutyFlow(dutyFlow);
							conn.setFlowItem(flowItem);
							conn.connect();
						}
					}
				}
			}
		}

		// Return the Office
		return office;
	}

	/**
	 * Stores the Office.
	 * 
	 * @param office
	 *            {@link OfficeModel} to store.
	 * @param configuration
	 *            Configuration to store the {@link OfficeModel} within.
	 * @throws Exception
	 *             If fails to store the {@link OfficeModel}.
	 */
	public void storeOffice(OfficeModel office, ConfigurationItem configuration)
			throws Exception {

		// Ensure teams linked in (for new links)
		for (ExternalTeamModel extTeam : office.getExternalTeams()) {
			for (FlowItemToTeamModel flowItem : extTeam.getFlowItems()) {
				flowItem.setTeamName(extTeam.getName());
			}
		}

		// Store the office
		this.modelRepository.store(office, configuration);
	}

	/**
	 * Loads the {@link OfficeRoomModel}.
	 * 
	 * @param rawRoomId
	 *            Id of the top level {@link RoomModel}.
	 * @param rawRoomName
	 *            Name of the top level {@link RoomModel}.
	 * @param rawRoom
	 *            Top level {@link RoomModel}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader} for loading the sub {@link DeskModel}
	 *            instances.
	 * @return {@link OfficeRoomModel}.
	 * @throws Exception
	 *             If fails to load the {@link OfficeRoomModel}.
	 */
	public OfficeRoomModel loadOfficeRoom(String rawRoomId, String rawRoomName,
			RoomModel rawRoom, ConfigurationContext context,
			ClassLoader classLoader) throws Exception {

		// Create the office room
		OfficeRoomModel officeRoom = new OfficeRoomModel();

		// Create the loaders
		RoomLoader roomLoader = new RoomLoader();
		DeskLoader deskLoader = new DeskLoader(classLoader);

		// Synchronise room
		RoomToOfficeRoomSynchroniser.synchroniseRoomOntoOfficeRoom(rawRoomId,
				rawRoomName, rawRoom, officeRoom);

		// Recursively load the sub rooms/desks
		this.recursiveLoadSubRooms(officeRoom, roomLoader, deskLoader, context);

		// Return the office room
		return officeRoom;
	}

	/**
	 * Loads the {@link OfficeRoomModel}.
	 * 
	 * @param officeRoom
	 *            {@link OfficeRoomModel}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws Exception
	 *             If fails to load the {@link OfficeRoomModel}.
	 */
	public void loadOfficeRoom(OfficeRoomModel officeRoom,
			ConfigurationContext context, ClassLoader classLoader)
			throws Exception {

		// Create the loaders
		RoomLoader roomLoader = new RoomLoader();
		DeskLoader deskLoader = new DeskLoader(classLoader);

		// Recursively load the sub rooms/desks
		this.recursiveLoadSubRooms(officeRoom, roomLoader, deskLoader, context);
	}

	/**
	 * Recursively loads the sub rooms of the input {@link OfficeRoomModel}.
	 * 
	 * @param room
	 *            Parent {@link OfficeRoomModel}.
	 * @param roomLoader
	 *            {@link RoomLoader}.
	 * @param deskLoader
	 *            {@link DeskModel}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to load the sub rooms/desks.
	 */
	private void recursiveLoadSubRooms(OfficeRoomModel room,
			RoomLoader roomLoader, DeskLoader deskLoader,
			ConfigurationContext context) throws Exception {

		// Load the sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {

			// Obtain the configuration item for the sub room
			String subRoomId = subRoom.getId();
			ConfigurationItem configItem = context
					.getConfigurationItem(subRoomId);

			// Ensure have configuration for sub room
			if (configItem == null) {
				// TODO provide error on room
				throw new UnsupportedOperationException(
						"TODO provide error on room if not find sub-room");
			}

			// Load the room model
			RoomModel actualRoom = roomLoader.loadRoom(configItem);

			// Synchronise room
			RoomToOfficeRoomSynchroniser.synchroniseRoomOntoOfficeRoom(
					subRoomId, subRoom.getName(), actualRoom, subRoom);

			// Recursive load the further sub rooms
			this
					.recursiveLoadSubRooms(subRoom, roomLoader, deskLoader,
							context);
		}

		// Load the desks
		for (OfficeDeskModel desk : room.getDesks()) {

			// Obtain the configuration item for the desk
			String deskId = desk.getId();
			ConfigurationItem configItem = context.getConfigurationItem(deskId);

			// Ensure have configuration for desk
			if (configItem == null) {
				// TODO provide error on room
				throw new UnsupportedOperationException(
						"TODO provide error on room if not find desk");
			}

			// Load the desk model
			DeskModel actualDesk = deskLoader.loadDesk(configItem);

			// Synchronise desk
			DeskToOfficeDeskSynchroniser.synchroniseDeskOntoOfficeDesk(deskId,
					actualDesk, desk);
		}
	}

	/**
	 * Loads the {@link OfficeRoomModel}.
	 * 
	 * @param room
	 *            {@link OfficeRoomModel}.
	 * @param teams
	 *            Registry of {@link ExternalTeamModel} instances.
	 * @param duties
	 *            Registry of {@link DutyModel} entries.
	 */
	private void linkInRoom(OfficeRoomModel room,
			Map<String, ExternalTeamModel> teams,
			DoubleKeyMap<String, String, DutyModel> duties) {

		// Ensure have a room
		if (room == null) {
			return;
		}

		// Recursively link in sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {
			this.linkInRoom(subRoom, teams, duties);
		}

		// Load the desks of the room
		for (OfficeDeskModel desk : room.getDesks()) {
			for (FlowItemModel flowItem : desk.getFlowItems()) {

				// Team of flow item
				FlowItemToTeamModel connTeam = flowItem.getTeam();
				if (connTeam != null) {
					ExternalTeamModel team = teams.get(connTeam.getTeamName());
					if (team != null) {
						connTeam.setFlowItem(flowItem);
						connTeam.setTeam(team);
						connTeam.connect();
					}
				}

				// Pre-administration duties
				for (FlowItemToPreAdministratorDutyModel connPreAdmin : flowItem
						.getPreAdminDutys()) {
					DutyModel duty = duties.get(connPreAdmin
							.getAdministratorId(), connPreAdmin.getDutyKey());
					if (duty != null) {
						connPreAdmin.setFlowItem(flowItem);
						connPreAdmin.setDuty(duty);
						connPreAdmin.connect();
					}
				}

				// Post-administration duties
				for (FlowItemToPostAdministratorDutyModel connPostAdmin : flowItem
						.getPostAdminDutys()) {
					DutyModel duty = duties.get(connPostAdmin
							.getAdministratorId(), connPostAdmin.getDutyKey());
					if (duty != null) {
						connPostAdmin.setFlowItem(flowItem);
						connPostAdmin.setDuty(duty);
						connPostAdmin.connect();
					}
				}
			}
		}
	}

	/**
	 * Registers all the {@link FlowItemModel} instances within the input
	 * registry.
	 * 
	 * @param room
	 *            {@link OfficeRoomModel} containing the {@link FlowItemModel}
	 *            instances.
	 * @param flowItems
	 *            Registry of {@link FlowItemModel} instances.
	 */
	private void registerFlowItems(OfficeRoomModel room,
			Map<String, FlowItemModel> flowItems) {

		// Ensure have a room
		if (room == null) {
			return;
		}

		// Recursively load sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {
			this.registerFlowItems(subRoom, flowItems);
		}

		// Register the flow items
		for (OfficeDeskModel desk : room.getDesks()) {
			for (FlowItemModel flowItem : desk.getFlowItems()) {
				flowItems.put(flowItem.getId(), flowItem);
			}
		}
	}

}
