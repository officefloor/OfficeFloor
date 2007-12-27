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
package net.officefloor.room;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.ManagedObjectToExternalManagedObjectModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link net.officefloor.model.room.RoomModel}.
 * 
 * @author Daniel
 */
public class RoomLoader {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Flag indicating if the {@link SubRoomModel} models have been registered.
	 */
	private boolean isModelsRegistered = false;

	/**
	 * Default constructor.
	 */
	public RoomLoader() {
		this(new ModelRepository());
	}

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public RoomLoader(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * Loads the {@link RoomModel} from configuration.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link RoomModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public RoomModel loadRoom(ConfigurationItem configuration) throws Exception {

		// Load the Room from configuration
		RoomModel room = this.modelRepository.retrieve(new RoomModel(),
				configuration);

		// Create the registry of inputs
		DoubleKeyMap<String, String, SubRoomInputFlowModel> inputs = new DoubleKeyMap<String, String, SubRoomInputFlowModel>();
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomInputFlowModel input : subRoom.getInputFlows()) {
				inputs.put(subRoom.getId(), input.getName(), input);
			}
		}

		// Connect the outputs to inputs
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomOutputFlowModel output : subRoom.getOutputFlows()) {
				OutputFlowToInputFlowModel conn = output.getInput();
				if (conn != null) {
					// Obtain the input
					SubRoomInputFlowModel input = inputs.get(conn
							.getSubRoomName(), conn.getInputFlowName());
					if (input != null) {
						// Connect
						conn.setOutput(output);
						conn.setInput(input);
						conn.connect();
					}
				}
			}
		}

		// Create the registry of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel externalFlow : room.getExternalFlows()) {
			externalFlows.put(externalFlow.getName(), externalFlow);
		}

		// Connect the outputs to external flows
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomOutputFlowModel output : subRoom.getOutputFlows()) {
				OutputFlowToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel externalFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (externalFlow != null) {
						// Connect
						conn.setOutput(output);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Create the registry of external managed objects
		Map<String, ExternalManagedObjectModel> externalMOs = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel externalMo : room
				.getExternalManagedObjects()) {
			externalMOs.put(externalMo.getName(), externalMo);
		}

		// Connect the managed objects to external managed objects
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomManagedObjectModel mo : subRoom.getManagedObjects()) {
				ManagedObjectToExternalManagedObjectModel conn = mo
						.getExternalManagedObject();
				if (conn != null) {
					// Obtain the external managed object
					ExternalManagedObjectModel extMo = externalMOs.get(conn
							.getName());
					if (extMo != null) {
						// Connect
						conn.setManagedObject(mo);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Return the room
		return room;
	}

	/**
	 * Stores the {@link RoomModel}.
	 * 
	 * @param room
	 *            {@link RoomModel} to be stored.
	 * @param configurationItem
	 *            {@link ConfigurationItem} to contain the persisted state of
	 *            the {@link RoomModel}.
	 * @throws Exception
	 *             If fails to store the {@link RoomModel}.
	 */
	public void storeRoom(RoomModel room, ConfigurationItem configurationItem)
			throws Exception {

		// Ensure managed objects linked in (for new links)
		for (ExternalManagedObjectModel extMo : room
				.getExternalManagedObjects()) {
			for (ManagedObjectToExternalManagedObjectModel link : extMo
					.getSubRoomManagedObjects()) {
				link.setName(extMo.getName());
			}
		}

		// Create the map of inputs to its sub room
		Map<SubRoomInputFlowModel, SubRoomModel> inputToRoom = new HashMap<SubRoomInputFlowModel, SubRoomModel>();
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomInputFlowModel input : subRoom.getInputFlows()) {
				inputToRoom.put(input, subRoom);
			}
		}

		// Ensure output flow items linked to input flow items
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomOutputFlowModel output : subRoom.getOutputFlows()) {
				OutputFlowToInputFlowModel conn = output.getInput();
				if (conn != null) {
					SubRoomInputFlowModel input = conn.getInput();
					conn.setSubRoomName(inputToRoom.get(input).getId());
					conn.setInputFlowName(input.getName());
				}
			}
		}

		// Ensure output flow items linked to external flows
		for (SubRoomModel subRoom : room.getSubRooms()) {
			for (SubRoomOutputFlowModel output : subRoom.getOutputFlows()) {
				OutputFlowToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = conn.getExternalFlow();
					conn.setExternalFlowName(externalFlow.getName());
				}
			}
		}

		// Stores the model
		this.modelRepository.store(room, configurationItem);
	}

	/**
	 * Loads the {@link SubRoomModel} from the input {@link ConfigurationItem}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the
	 *            {@link net.officefloor.model.desk.DeskModel} or
	 *            {@link RoomModel}.
	 * @return {@link SubRoomModel}.
	 * @throws Exception
	 *             If fails to load the {@link SubRoomModel}.
	 */
	public SubRoomModel loadSubRoom(ConfigurationItem configurationItem)
			throws Exception {

		// Ensure the sub room models registered
		synchronized (this) {
			if (!this.isModelsRegistered) {
				this.modelRepository.registerModel(DeskModel.class);
				this.modelRepository.registerModel(RoomModel.class);

				// Models now registered
				this.isModelsRegistered = true;
			}
		}

		// Unmarshall the desk/room model
		Object model = this.modelRepository.retrieve(configurationItem);

		// Handle based on model type
		SubRoomModel subRoom = new SubRoomModel();
		if (model instanceof DeskModel) {
			// Synchronise the desk onto the model
			DeskModel desk = (DeskModel) model;
			DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);
			subRoom.setDesk(configurationItem.getId());

		} else if (model instanceof RoomModel) {
			// Synchronise the room onto the model
			RoomModel room = (RoomModel) model;
			RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);
			subRoom.setRoom(configurationItem.getId());

		} else {
			// Unknown model
			throw new Exception("Unknown retrieved model of type "
					+ (model == null ? null : model.getClass().getName()));
		}

		// Return the sub room
		return subRoom;
	}
}
