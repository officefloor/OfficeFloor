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
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link net.officefloor.model.section.SectionModel}.
 * 
 * @author Daniel
 */
public class RoomLoader {

	/**
	 * {@link ModelRepositoryImpl}.
	 */
	private final ModelRepositoryImpl modelRepository;

	/**
	 * Flag indicating if the {@link SubSectionModel} models have been registered.
	 */
	private boolean isModelsRegistered = false;

	/**
	 * Default constructor.
	 */
	public RoomLoader() {
		this(new ModelRepositoryImpl());
	}

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepositoryImpl}.
	 */
	public RoomLoader(ModelRepositoryImpl modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * Loads the {@link SectionModel} from configuration.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link SectionModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public SectionModel loadRoom(ConfigurationItem configuration) throws Exception {

		// Load the Room from configuration
		SectionModel room = this.modelRepository.retrieve(new SectionModel(),
				configuration);

		// Create the registry of inputs
		DoubleKeyMap<String, String, SubSectionInputModel> inputs = new DoubleKeyMap<String, String, SubSectionInputModel>();
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionInputModel input : subRoom.getSubSectionInputs()) {
				inputs.put(subRoom.getSubSectionName(), input.getSubSectionInputName(), input);
			}
		}

		// Connect the outputs to inputs
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionOutputModel output : subRoom.getSubSectionOutputs()) {
				SubSectionOutputToSubSectionInputModel conn = output.getSubSectionInput();
				if (conn != null) {
					// Obtain the input
					SubSectionInputModel input = inputs.get(conn
							.getSubSectionName(), conn.getSubSectionInputName());
					if (input != null) {
						// Connect
						conn.setSubSectionOutput(output);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Create the registry of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel externalFlow : room.getExternalFlows()) {
			externalFlows.put(externalFlow.getExternalFlowName(), externalFlow);
		}

		// Connect the outputs to external flows
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionOutputModel output : subRoom.getSubSectionOutputs()) {
				SubSectionOutputToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel externalFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (externalFlow != null) {
						// Connect
						conn.setSubSectionOutput(output);
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
			externalMOs.put(externalMo.getExternalManagedObjectName(), externalMo);
		}

		// Connect the managed objects to external managed objects
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionObjectModel mo : subRoom.getSubSectionObjects()) {
				SubSectionObjectToExternalManagedObjectModel conn = mo
						.getExternalManagedObject();
				if (conn != null) {
					// Obtain the external managed object
					ExternalManagedObjectModel extMo = externalMOs.get(conn
							.getExternalManagedObjectName());
					if (extMo != null) {
						// Connect
						conn.setSubSectionObject(mo);
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
	 * Stores the {@link SectionModel}.
	 * 
	 * @param room
	 *            {@link SectionModel} to be stored.
	 * @param configurationItem
	 *            {@link ConfigurationItem} to contain the persisted state of
	 *            the {@link SectionModel}.
	 * @throws Exception
	 *             If fails to store the {@link SectionModel}.
	 */
	public void storeRoom(SectionModel room, ConfigurationItem configurationItem)
			throws Exception {

		// Ensure managed objects linked in (for new links)
		for (ExternalManagedObjectModel extMo : room
				.getExternalManagedObjects()) {
			for (SubSectionObjectToExternalManagedObjectModel link : extMo
					.getSubSectionObjects()) {
				link.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Create the map of inputs to its sub room
		Map<SubSectionInputModel, SubSectionModel> inputToRoom = new HashMap<SubSectionInputModel, SubSectionModel>();
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionInputModel input : subRoom.getSubSectionInputs()) {
				inputToRoom.put(input, subRoom);
			}
		}

		// Ensure output flow items linked to input flow items
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionOutputModel output : subRoom.getSubSectionOutputs()) {
				SubSectionOutputToSubSectionInputModel conn = output.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = conn.getSubSectionInput();
					conn.setSubSectionName(inputToRoom.get(input).getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Ensure output flow items linked to external flows
		for (SubSectionModel subRoom : room.getSubSections()) {
			for (SubSectionOutputModel output : subRoom.getSubSectionOutputs()) {
				SubSectionOutputToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = conn.getExternalFlow();
					conn.setExternalFlowName(externalFlow.getExternalFlowName());
				}
			}
		}

		// Stores the model
		this.modelRepository.store(room, configurationItem);
	}

	/**
	 * Loads the {@link SubSectionModel} from the input {@link ConfigurationItem}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the
	 *            {@link net.officefloor.model.desk.DeskModel} or
	 *            {@link SectionModel}.
	 * @return {@link SubSectionModel}.
	 * @throws Exception
	 *             If fails to load the {@link SubSectionModel}.
	 */
	public SubSectionModel loadSubRoom(ConfigurationItem configurationItem)
			throws Exception {
		// Return the loaded sub room
		return this.loadSubRoom(new SubSectionModel(), configurationItem);
	}

	/**
	 * <p>
	 * Loads the {@link SubSectionModel} from the {@link ConfigurationContext}.
	 * <p>
	 * Utilises the location on the {@link SubSectionModel}.
	 * 
	 * @param subRoom
	 *            {@link SubSectionModel}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to load the {@link SubSectionModel}.
	 */
	public void loadSubRoom(SubSectionModel subRoom, ConfigurationContext context)
			throws Exception {

		// Obtain the configuration
		String configurationLocation = (subRoom.getDeskLocation() == null ? subRoom
				.getSectionLocation() : subRoom.getDeskLocation());
		ConfigurationItem configurationItem = context
				.getConfigurationItem(configurationLocation);

		// Load the sub room
		this.loadSubRoom(subRoom, configurationItem);
	}

	/**
	 * Loads the {@link SubSectionModel}.
	 * 
	 * @param subRoom
	 *            {@link SubSectionModel}.
	 * @param configurationItem
	 *            {@link ConfigurationItem} for the {@link SubSectionModel}.
	 * @return Loaded {@link SubSectionModel}.
	 * @throws Exception
	 *             If fails to load the {@link SubSectionModel}.
	 */
	private SubSectionModel loadSubRoom(SubSectionModel subRoom,
			ConfigurationItem configurationItem) throws Exception {

		// Ensure the sub room models registered
		synchronized (this) {
			if (!this.isModelsRegistered) {
				this.modelRepository.registerModel(DeskModel.class);
				this.modelRepository.registerModel(SectionModel.class);

				// Models now registered
				this.isModelsRegistered = true;
			}
		}

		// Unmarshal the desk/room model
		Object model = this.modelRepository.retrieve(configurationItem);

		// Handle based on model type
		if (model instanceof DeskModel) {
			// Synchronise the desk onto the model
			DeskModel desk = (DeskModel) model;
			DeskToSubRoomSynchroniser.synchroniseDeskOntoSubRoom(desk, subRoom);
			subRoom.setDeskLocation(configurationItem.getLocation());

		} else if (model instanceof SectionModel) {
			// Synchronise the room onto the model
			SectionModel room = (SectionModel) model;
			RoomToSubRoomSynchroniser.synchroniseRoomOntoSubRoom(room, subRoom);
			subRoom.setSectionLocation(configurationItem.getLocation());

		} else {
			// Unknown model
			throw new Exception("Unknown retrieved model of type "
					+ (model == null ? null : model.getClass().getName()));
		}

		// Return the sub room
		return subRoom;
	}
}
