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

import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.OfficeDeskModel;

/**
 * Synchronises the {@link DeskModel} onto the {@link OfficeDeskModel}.
 * 
 * @author Daniel
 */
public class DeskToOfficeDeskSynchroniser {

	/**
	 * Synchronises the {@link DeskModel} onto the {@link OfficeDeskModel}.
	 * 
	 * @param deskId
	 *            Id of the desk.
	 * @param desk
	 *            {@link DeskModel}.
	 * @param officeDesk
	 *            {@link OfficeDeskModel}.
	 */
	public static void synchroniseDeskOntoOfficeDesk(String deskId,
			DeskModel desk, OfficeDeskModel officeDesk) {

		// Specify id of the desk
		officeDesk.setId(deskId);

		// Create the map of existing public flows
		Map<String, FlowItemModel> existingFlowItems = new HashMap<String, FlowItemModel>();
		for (FlowItemModel flowItem : officeDesk.getFlowItems()) {
			existingFlowItems.put(flowItem.getId(), flowItem);
		}

		// Synchronise the flow items
		for (net.officefloor.model.desk.FlowItemModel flowItem : desk
				.getFlowItems()) {
			String flowItemId = flowItem.getId();
			if (existingFlowItems.containsKey(flowItemId)) {
				// Remove from existing (so not removed later)
				existingFlowItems.remove(flowItemId);
			} else {
				// Add the flow item
				FlowItemModel newFlowItem = new FlowItemModel(flowItemId,
						flowItem.getTaskName(), null, null, null, null);
				officeDesk.addFlowItem(newFlowItem);
			}
		}

		// Remove the old flow items
		for (FlowItemModel oldFlowItem : existingFlowItems.values()) {
			officeDesk.removeFlowItem(oldFlowItem);
		}
	}

	/**
	 * Access via static methods.
	 */
	private DeskToOfficeDeskSynchroniser() {
	}
}
