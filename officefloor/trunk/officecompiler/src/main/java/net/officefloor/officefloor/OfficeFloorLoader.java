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
package net.officefloor.officefloor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.LinkProcessToOfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.ManagedObjectTeamToTeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeManagedObjectToManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.OfficeTeamToTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorLoader {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Default constructor.
	 */
	public OfficeFloorLoader() {
		this.modelRepository = new ModelRepository();
	}

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public OfficeFloorLoader(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * Loads the {@link OfficeFloorModel} from the configuration.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public OfficeFloorModel loadOfficeFloor(ConfigurationItem configuration)
			throws Exception {

		// Load the office floor from the configuration
		OfficeFloorModel officeFloor = this.modelRepository.retrieve(
				new OfficeFloorModel(), configuration);

		// Create the registry of offices
		Map<String, OfficeFloorOfficeModel> offices = new HashMap<String, OfficeFloorOfficeModel>();
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {
			offices.put(office.getName(), office);
		}

		// Create the registry of managed object sources
		Map<String, ManagedObjectSourceModel> mosRegistry = new HashMap<String, ManagedObjectSourceModel>();
		for (ManagedObjectSourceModel mos : officeFloor
				.getManagedObjectSources()) {
			mosRegistry.put(mos.getId(), mos);
		}

		// Create the registry of teams
		Map<String, TeamModel> teams = new HashMap<String, TeamModel>();
		for (TeamModel team : officeFloor.getTeams()) {
			teams.put(team.getId(), team);
		}

		// Connect the office connections
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {

			// Connect the managed object sources
			for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
				OfficeManagedObjectToManagedObjectSourceModel conn = mo
						.getManagedObjectSource();
				if (conn != null) {
					ManagedObjectSourceModel mos = mosRegistry.get(conn
							.getManagedObjectSourceId());
					if (mos != null) {
						conn.setOfficeManagedObject(mo);
						conn.setManagedObjectSource(mos);
						conn.connect();
					}
				}
			}

			// Connect the teams
			for (OfficeTeamModel officeTeam : office.getTeams()) {
				OfficeTeamToTeamModel conn = officeTeam.getTeam();
				if (conn != null) {
					TeamModel team = teams.get(conn.getTeamId());
					if (team != null) {
						conn.setOfficeTeam(officeTeam);
						conn.setTeam(team);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source connections
		for (ManagedObjectSourceModel mos : officeFloor
				.getManagedObjectSources()) {

			// Map of office tasks
			DoubleKeyMap<String, String, OfficeTaskModel> officeTasks = new DoubleKeyMap<String, String, OfficeTaskModel>();

			// Connect the managing offices
			ManagedObjectSourceToOfficeFloorOfficeModel conn = mos
					.getManagingOffice();
			if (conn != null) {
				OfficeFloorOfficeModel office = offices.get(conn
						.getManagingOfficeName());
				if (office != null) {
					conn.setManagedObjectSource(mos);
					conn.setManagingOffice(office);
					conn.connect();

					// Register the tasks of the office
					for (OfficeTaskModel officeTask : office.getTasks()) {
						officeTasks.put(officeTask.getWorkName(), officeTask
								.getTaskName(), officeTask);
					}
				}
			}

			// Connect the teams
			for (ManagedObjectTeamModel moTeam : mos.getTeams()) {
				ManagedObjectTeamToTeamModel teamConn = moTeam.getTeam();
				if (teamConn != null) {
					TeamModel team = teams.get(teamConn.getTeamId());
					if (team != null) {
						teamConn.setManagedObjectTeam(moTeam);
						teamConn.setTeam(team);
						teamConn.connect();
					}
				}
			}

			// Connect the handler link processes to office tasks
			for (ManagedObjectHandlerModel handler : mos.getHandlers()) {
				ManagedObjectHandlerInstanceModel handlerInstance = handler
						.getHandlerInstance();
				if (handlerInstance != null) {
					for (ManagedObjectHandlerLinkProcessModel linkProcess : handlerInstance
							.getLinkProcesses()) {
						LinkProcessToOfficeTaskModel taskConn = linkProcess
								.getOfficeTask();
						if (taskConn != null) {
							OfficeTaskModel officeTask = officeTasks.get(
									taskConn.getWorkName(), taskConn
											.getTaskName());
							if (officeTask != null) {
								taskConn.setLinkProcess(linkProcess);
								taskConn.setOfficeTask(officeTask);
								taskConn.connect();
							}
						}
					}
				}
			}
		}

		// Return the office floor
		return officeFloor;
	}

	/**
	 * Stores the {@link OfficeFloorModel}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloorModel} to store.
	 * @param configurationItem
	 *            {@link ConfigurationItem} to contain the stored
	 *            {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeFloorModel}.
	 */
	public void storeOfficeFloor(OfficeFloorModel officeFloor,
			ConfigurationItem configurationItem) throws Exception {

		// Ensure the teams are linked
		for (TeamModel teamModel : officeFloor.getTeams()) {
			for (OfficeTeamToTeamModel conn : teamModel.getOfficeTeams()) {
				conn.setTeamId(teamModel.getId());
			}
			for (ManagedObjectTeamToTeamModel conn : teamModel
					.getManagedObjectTeams()) {
				conn.setTeamId(teamModel.getId());
			}
		}

		// Ensure the managed object sources are linked
		for (ManagedObjectSourceModel managedObjectSource : officeFloor
				.getManagedObjectSources()) {
			for (OfficeManagedObjectToManagedObjectSourceModel conn : managedObjectSource
					.getOfficeManagedObjects()) {
				conn.setManagedObjectSourceId(managedObjectSource.getId());
			}
		}

		// Ensure the tasks are linked
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {
			for (OfficeTaskModel task : office.getTasks()) {
				for (LinkProcessToOfficeTaskModel conn : task
						.getLinkProcesses()) {
					conn.setWorkName(task.getWorkName());
					conn.setTaskName(task.getTaskName());
				}
			}
		}

		// Ensure the responsible offices are linked
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {
			for (ManagedObjectSourceToOfficeFloorOfficeModel conn : office
					.getResponsibleManagedObjects()) {
				conn.setManagingOfficeName(office.getName());
			}
		}

		// Store the model
		this.modelRepository.store(officeFloor, configurationItem);
	}

	/**
	 * Loads the {@link OfficeFloorOfficeModel} from the input
	 * {@link ConfigurationItem} for a {@link OfficeModel}.
	 * 
	 * @param configurationItem
	 *            {@link OfficeModel} {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to load the {@link OfficeFloorOfficeModel}.
	 */
	public OfficeFloorOfficeModel loadOfficeFloorOffice(
			ConfigurationItem configurationItem) throws Exception {

		// Load the Office
		OfficeLoader officeLoader = new OfficeLoader();
		OfficeModel office = officeLoader.loadOffice(configurationItem);

		// Create the office floor office
		OfficeFloorOfficeModel officeFloorOffice = new OfficeFloorOfficeModel();

		// Synchronise the office onto the office floor office
		OfficeToOfficeFloorOfficeSynchroniser
				.synchroniseOfficeOntoOfficeFloorOffice(configurationItem
						.getId(), office, officeFloorOffice);

		// Return the office floor office
		return officeFloorOffice;
	}

	/**
	 * Loads the {@link OfficeTaskModel} instances.
	 * 
	 * @param office
	 *            {@link OfficeFloorOfficeModel}.
	 * @param configurationContext
	 *            {@link ConfigurationContext} to find the configuration of the
	 *            underlying {@link OfficeModel}.
	 * @return Listing of {@link OfficeTaskModel} instances for the input
	 *         {@link OfficeFloorOfficeModel}.
	 * @throws Exception
	 *             If fails to load the {@link OfficeTaskModel} instances.
	 */
	public OfficeTaskModel[] loadOfficeTasks(OfficeFloorOfficeModel office,
			ConfigurationContext configurationContext) throws Exception {

		// Obtain the configuration to the office
		String officeLocation = office.getId();
		ConfigurationItem officeConfiguration = configurationContext
				.getConfigurationItem(officeLocation);

		// Obtain the office
		OfficeModel officeModel = new OfficeLoader()
				.loadOffice(officeConfiguration);

		// Obtain the list of tasks
		List<OfficeTaskModel> tasks = new LinkedList<OfficeTaskModel>();
		OfficeRoomModel room = officeModel.getRoom();
		if (room != null) {
			this.loadOfficeTasks(room, "", tasks);
		}

		// Return the tasks
		return tasks.toArray(new OfficeTaskModel[0]);
	}

	/**
	 * Loads the {@link OfficeTaskModel} instances of the
	 * {@link OfficeRoomModel}.
	 * 
	 * @param room
	 *            {@link OfficeRoomModel}.
	 * @param workPrefix
	 *            {@link Work} prefix name.
	 * @param tasks
	 *            Listing of {@link OfficeTaskModel} instances.
	 */
	private void loadOfficeTasks(OfficeRoomModel room, String workPrefix,
			List<OfficeTaskModel> tasks) {

		// Specify the work prefix
		workPrefix = (workPrefix.length() == 0 ? "" : workPrefix + ".");

		// Recursively load the tasks of the sub rooms
		for (OfficeRoomModel subRoom : room.getSubRooms()) {
			String workName = workPrefix + subRoom.getName();
			this.loadOfficeTasks(subRoom, workName, tasks);
		}

		// Load the tasks of the desks
		for (OfficeDeskModel desk : room.getDesks()) {
			String workName = workPrefix + desk.getName();
			this.loadOfficeTasks(desk, workName, tasks);
		}
	}

	/**
	 * Loads the {@link OfficeTaskModel} instances of the
	 * {@link OfficeDeskModel}.
	 * 
	 * @param desk
	 *            {@link OfficeDeskModel}.
	 * @param workPrefix
	 *            {@link Work} prefix name.
	 * @param tasks
	 *            Listing of {@link OfficeTaskModel} instances.
	 */
	private void loadOfficeTasks(OfficeDeskModel desk, String workPrefix,
			List<OfficeTaskModel> tasks) {

		// Specify the work prefix
		workPrefix = (workPrefix.length() == 0 ? "" : workPrefix + ".");

		// Load the tasks of the desk
		for (FlowItemModel flowItem : desk.getFlowItems()) {

			// Obtain initial task details of flow
			String workName = workPrefix + flowItem.getWorkName();
			String taskName = flowItem.getTaskName();

			// Add the office task
			tasks.add(new OfficeTaskModel(workName, taskName, null));
		}
	}

}
