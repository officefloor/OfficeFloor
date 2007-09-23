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
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link net.officefloor.model.office.OfficeModel}.
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

		// Load the Room and its sub rooms
		this.loadRoom(office.getRoom(), teams, duties);

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
		this.modelRepository.store(office, configuration);
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
	private void loadRoom(OfficeRoomModel room,
			Map<String, ExternalTeamModel> teams,
			DoubleKeyMap<String, String, DutyModel> duties) {

		// Ensure have a room
		if (room == null) {
			return;
		}

		// Recursively load sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {
			this.loadRoom(subRoom, teams, duties);
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
