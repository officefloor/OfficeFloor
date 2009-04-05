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
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;

/**
 * Synchronises the {@link net.officefloor.model.desk.DeskModel} to the
 * {@link net.officefloor.model.section.SubSectionModel}.
 * 
 * @author Daniel
 */
public class DeskToSubRoomSynchroniser {

	/**
	 * Synchronises the {@link DeskModel} onto the {@link SubSectionModel}.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 * @param subRoom
	 *            {@link SubSectionModel}.
	 */
	public static void synchroniseDeskOntoSubRoom(DeskModel desk,
			SubSectionModel subRoom) {

		// Create the map of existing managed objects to their names
		Map<String, SubSectionObjectModel> existingManagedObjects = new HashMap<String, SubSectionObjectModel>();
		for (SubSectionObjectModel mo : subRoom.getSubSectionObjects()) {
			existingManagedObjects.put(mo.getSubSectionObjectName(), mo);
		}

		// Add managed objects as per desk
		for (ExternalManagedObjectModel deskMo : desk
				.getExternalManagedObjects()) {

			// Obtain the managed object
			SubSectionObjectModel mo = existingManagedObjects.get(deskMo
					.getExternalManagedObjectName());
			if (mo == null) {
				// Not exist therefore create and add
				mo = new SubSectionObjectModel(deskMo
						.getExternalManagedObjectName(),
						deskMo.getObjectType(), null);
				subRoom.addSubSectionObject(mo);
			}

			// Remove from existing list
			existingManagedObjects
					.remove(deskMo.getExternalManagedObjectName());
		}

		// Remove no longer existing managed objects
		for (SubSectionObjectModel mo : existingManagedObjects.values()) {
			subRoom.removeSubSectionObject(mo);
		}

		// Create the map of existing input flows to their names
		Map<String, SubSectionInputModel> existingInputFlows = new HashMap<String, SubSectionInputModel>();
		for (SubSectionInputModel inFlow : subRoom.getSubSectionInputs()) {
			existingInputFlows.put(inFlow.getSubSectionInputName(), inFlow);
		}

		// Add input flows as per desk
		for (TaskModel flow : desk.getTasks()) {

			// Do not add non-public flow items
			if (!flow.getIsPublic()) {
				continue;
			}

			// Obtain the input flow
			SubSectionInputModel inFlow = existingInputFlows.get(flow
					.getTaskName());
			if (inFlow == null) {
				// Not exist therefore create and add (defaultly not public)
				inFlow = new SubSectionInputModel(flow.getTaskName(),
						Object.class.getName(), false, null);
				subRoom.addSubSectionInput(inFlow);
			}

			// Remove from existing list
			existingInputFlows.remove(flow.getTaskName());
		}

		// Remove no longer existing input flows
		for (SubSectionInputModel inFlow : existingInputFlows.values()) {
			subRoom.removeSubSectionInput(inFlow);
		}

		// Create the map of existing output flows to their names
		Map<String, SubSectionOutputModel> existingOutputFlows = new HashMap<String, SubSectionOutputModel>();
		for (SubSectionOutputModel outFlow : subRoom.getSubSectionOutputs()) {
			existingOutputFlows.put(outFlow.getSubSectionOutputName(), outFlow);
		}

		// Add output flows as per desk
		for (ExternalFlowModel flow : desk.getExternalFlows()) {

			// Obtain the output flow
			SubSectionOutputModel outFlow = existingOutputFlows.get(flow
					.getExternalFlowName());
			if (outFlow == null) {
				// Not exist therefore create and add
				outFlow = new SubSectionOutputModel(flow.getExternalFlowName(),
						Object.class.getName());
				subRoom.addSubSectionOutput(outFlow);
			}

			// Remove from existing list
			existingOutputFlows.remove(outFlow.getSubSectionOutputName());
		}

		// Remove no longer existing output flows
		for (SubSectionOutputModel outFlow : existingOutputFlows.values()) {
			subRoom.removeSubSectionOutput(outFlow);
		}

	}

	/**
	 * Access via static methods.
	 */
	private DeskToSubRoomSynchroniser() {
	}

}
