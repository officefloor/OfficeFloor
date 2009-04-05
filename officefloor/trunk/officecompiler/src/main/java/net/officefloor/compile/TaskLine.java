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

import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.util.OFCU;

/**
 * Contains the various items on the line from a {@link TaskModel} through
 * to its {@link net.officefloor.model.office.FlowItemModel} and subsequent
 * {@link TeamModel} responsible for the {@link Task}.
 * 
 * @author Daniel
 */
public class TaskLine<W extends Work> {

	/**
	 * Desk {@link TaskModel}.
	 */
	public final TaskModel deskFlowItem;

	/**
	 * {@link TaskEntry} of the {@link TaskModel}.
	 */
	public final TaskEntry<W> taskEntry;

	/**
	 * {@link WorkTaskModel} for the {@link TaskModel}.
	 */
	public final WorkTaskModel deskTask;

	/**
	 * {@link WorkEntry}.
	 */
	public final WorkEntry<W> workEntry;

	/**
	 * {@link DeskEntry}.
	 */
	public final DeskEntry deskEntry;

	/**
	 * {@link OfficeEntry}.
	 */
	public final OfficeEntry officeEntry;

	/**
	 * {@link OfficeDeskModel}.
	 */
	public final OfficeDeskModel officeDesk;

	/**
	 * {@link Office} {@link net.officefloor.model.office.FlowItemModel}.
	 */
	public final net.officefloor.model.office.FlowItemModel officeFlowItem;

	/**
	 * {@link Office} {@link ExternalTeamModel} responsible for the {@link Task}.
	 */
	public final ExternalTeamModel officeExternalTeam;

	/**
	 * {@link OfficeFloorOfficeModel}.
	 */
	public final OfficeFloorOfficeModel officeFloorOffice;

	/**
	 * {@link OfficeFloorEntry}.
	 */
	public final OfficeFloorEntry officeFloorEntry;

	/**
	 * {@link OfficeTeamModel}.
	 */
	public final OfficeTeamModel officeTeam;

	/**
	 * {@link TeamModel} responsible for the {@link Task}.
	 */
	public final TeamModel team;

	/**
	 * Initiate the line from the {@link TaskModel}.
	 * 
	 * @param deskFlowItem
	 *            {@link TaskModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} for the {@link TaskModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public TaskLine(TaskModel deskFlowItem, TaskEntry<W> taskEntry)
			throws Exception {

		// Store starting point
		this.deskFlowItem = deskFlowItem;
		this.taskEntry = taskEntry;

		// Obtain the work and desk entry
		this.workEntry = this.taskEntry.getWorkEntry();
		this.deskEntry = this.workEntry.getDeskEntry();

		// Obtain the desk task
		this.deskTask = this.deskFlowItem.getWorkTask().getWorkTask();

		// Create the hierarchy of desk/room names
		Deque<String> hierarchy = new LinkedList<String>();
		hierarchy.push(this.deskEntry.getDeskName());
		RoomEntry roomEntry = this.deskEntry.getParentRoom();
		OfficeEntry officeEntry = null;
		while (roomEntry != null) {
			hierarchy.push(roomEntry.getRoomName());
			officeEntry = roomEntry.getOffice();
			roomEntry = roomEntry.getParentRoom();
		}
		this.officeEntry = officeEntry;

		// Obtain the external team on the office
		OfficeRoomModel officeRoom = null;
		OfficeDeskModel officeDesk = null;
		while (!hierarchy.isEmpty()) {

			// Obtain the next item down in the hierarchy
			String itemName = hierarchy.pop();

			// Specify based on location
			if (officeDesk != null) {
				// Hierarchy should be empty when have desk
				throw new Exception("Hierarchy of office "
						+ this.officeEntry.getId()
						+ " is out of sync for work "
						+ this.workEntry.getCanonicalWorkName() + " [task "
						+ this.taskEntry.getId() + "]");
			} else if (officeRoom == null) {
				// Top level room
				officeRoom = officeEntry.getModel().getRoom();
			} else {
				// Find the sub room by the hierarchy
				OfficeRoomModel childRoom = null;
				for (OfficeRoomModel subRoom : officeRoom.getSubRooms()) {
					if (itemName.equals(subRoom.getName())) {
						childRoom = subRoom;
					}
				}

				// Handle based on whether a room
				if (childRoom != null) {
					// Child is a room
					officeRoom = childRoom;
				} else {
					// Not a room therefore must be a desk
					for (OfficeDeskModel subRoom : officeRoom.getDesks()) {
						if (itemName.equals(subRoom.getName())) {
							officeDesk = subRoom;
						}
					}

					// Ensure have the desk
					if (officeDesk == null) {
						throw new Exception("Hierarchy of office "
								+ officeEntry.getId()
								+ " is out of sync for work "
								+ this.workEntry.getCanonicalWorkName()
								+ " [task " + this.taskEntry.getId() + "]");
					}
				}
			}
		}
		this.officeDesk = officeDesk;

		// Have office desk so find task on it
		net.officefloor.model.office.FlowItemModel officeFlowItem = null;
		for (net.officefloor.model.office.FlowItemModel of : officeDesk
				.getFlowItems()) {
			if (this.deskFlowItem.getTaskName().equals(of.getId())) {
				officeFlowItem = of;
			}
		}
		if (officeFlowItem == null) {
			throw new Exception("No corresponding flow item "
					+ this.deskFlowItem.getTaskName() + " on office desk "
					+ officeDesk.getName() + " of office "
					+ officeEntry.getId());
		}
		this.officeFlowItem = officeFlowItem;

		// Obtain the external office team
		this.officeExternalTeam = OFCU.get(this.officeFlowItem.getTeam(),
				"No team for ${0}", officeFlowItem.getTaskName()).getTeam();

		// Obtain the office within the office floor
		this.officeFloorEntry = this.officeEntry.getOfficeFloorEntry();
		this.officeFloorOffice = officeFloorEntry
				.getOfficeFloorOfficeModel(officeEntry);

		// Obtain the office floor office team
		OfficeTeamModel officeTeam = null;
		for (OfficeTeamModel team : this.officeFloorOffice.getTeams()) {
			if (this.officeExternalTeam.getName().equals(team.getTeamName())) {
				officeTeam = team;
			}
		}
		if (officeTeam == null) {
			throw new Exception("Can not find team '"
					+ this.officeExternalTeam.getName() + "' for office "
					+ this.officeFloorOffice.getId() + " in office floor "
					+ this.officeFloorEntry.getId());
		}
		this.officeTeam = officeTeam;

		// Obtain the team responsible for the task
		this.team = this.officeTeam.getTeam().getTeam();
	}
}
