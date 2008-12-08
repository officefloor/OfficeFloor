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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.room.EscalationToExternalEscalationModel;
import net.officefloor.model.room.EscalationToInputFlowModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;

/**
 * Tests the {@link FlowEscalationLine}.
 * 
 * @author Daniel
 */
public class FlowEscalationLineTest extends OfficeFrameTestCase {

	/**
	 * Ensure handle by same {@link Work}.
	 */
	public void testHandledBySameWork() throws Exception {

		// Create the entries
		DeskEntry deskEntry = EntryUtil.createDeskEntry(null, null);
		WorkEntry<Work> workEntry = EntryUtil.createWorkEntry("WORK",
				Work.class, deskEntry);
		TaskEntry<Work> taskEscalating = EntryUtil.createTaskEntry(
				"TASK_ESCALTING", workEntry);
		TaskEntry<Work> taskHandling = EntryUtil.createTaskEntry(
				"TASK_HANDLING", workEntry);
		FlowItemEscalationModel escalation = EntryUtil
				.createFlowItemEscalation(Exception.class, taskEscalating);

		// Link escalation to handling task
		new FlowItemEscalationToFlowItemModel(null, escalation, taskHandling
				.getModel()).connect();

		// Create the flow escalation line
		FlowEscalationLine line = new FlowEscalationLine(escalation,
				taskEscalating);

		// Verify line
		this.verifyFlowEscalationLine(line, true, escalation, null,
				taskEscalating, workEntry, deskEntry, taskHandling.getModel(),
				taskHandling, workEntry, deskEntry, false);
	}

	/**
	 * Ensure handle by different {@link Work} on the same {@link DeskModel}.
	 */
	public void testHandledByDifferentWorkOnSameDesk() throws Exception {

		// Create the entries
		DeskEntry deskEntry = EntryUtil.createDeskEntry(null, null);
		WorkEntry<Work> workEscalating = EntryUtil.createWorkEntry(
				"WORK_ESCALATING", Work.class, deskEntry);
		TaskEntry<Work> taskEscalating = EntryUtil.createTaskEntry(
				"TASK_ESCALATING", workEscalating);
		FlowItemEscalationModel escalation = EntryUtil
				.createFlowItemEscalation(Exception.class, taskEscalating);
		WorkEntry<Work> workHandling = EntryUtil.createWorkEntry(
				"WORK_HANDLING", Work.class, deskEntry);
		TaskEntry<Work> taskHandling = EntryUtil.createTaskEntry(
				"TASK_HANDLING", workHandling);

		// Link escalation to handling task
		new FlowItemEscalationToFlowItemModel(null, escalation, taskHandling
				.getModel()).connect();

		// Create the flow escalation line
		FlowEscalationLine line = new FlowEscalationLine(escalation,
				taskEscalating);

		// Verify line
		this.verifyFlowEscalationLine(line, false, escalation, null,
				taskEscalating, workEscalating, deskEntry, taskHandling
						.getModel(), taskHandling, workHandling, deskEntry,
				false);
	}

	/**
	 * Ensure handled by another {@link DeskModel}.
	 */
	public void testHandledByAnotherDesk() throws Exception {

		// Create the entries
		RoomEntry roomEntry = EntryUtil.createRoomEntry(null, null);

		// Escalating
		DeskEntry deskEscalating = EntryUtil.createDeskEntry("DESK_ESCALATING",
				roomEntry);
		WorkEntry<Work> workEscalating = EntryUtil.createWorkEntry(
				"WORK_ESCALATING", Work.class, deskEscalating);
		TaskEntry<Work> taskEscalating = EntryUtil.createTaskEntry(
				"TASK_ESCALATING", workEscalating);
		FlowItemEscalationModel escalation = EntryUtil
				.createFlowItemEscalation(Exception.class, taskEscalating);
		ExternalEscalationModel externalEscalation = EntryUtil
				.createExternalEscalation("ESCALATION", Exception.class,
						deskEscalating);
		new FlowItemEscalationToExternalEscalationModel(null, escalation,
				externalEscalation).connect();
		SubRoomEscalationModel subRoomEscalation = EntryUtil
				.createSubRoomEscalation(externalEscalation, deskEscalating);

		// Handling
		DeskEntry deskHandling = EntryUtil.createDeskEntry("DESK_HANDLING",
				roomEntry);
		WorkEntry<Work> workHandling = EntryUtil.createWorkEntry(
				"WORK_HANDLING", Work.class, deskHandling);
		TaskEntry<Work> taskHandling = EntryUtil.createTaskEntry(
				"TASK_HANDLING", workHandling);
		SubRoomInputFlowModel inputFlowHandling = EntryUtil
				.createSubRoomInputFlow(taskHandling.getModel(), deskHandling);

		// Link the escalation to handling task
		new EscalationToInputFlowModel(null, null, subRoomEscalation,
				inputFlowHandling).connect();

		// Create the flow escalation line
		FlowEscalationLine line = new FlowEscalationLine(escalation,
				taskEscalating);

		// Verify line
		this.verifyFlowEscalationLine(line, false, escalation,
				externalEscalation, taskEscalating, workEscalating,
				deskEscalating, taskHandling.getModel(), taskHandling,
				workHandling, deskHandling, false);
	}

	/**
	 * Ensures indicates handled by top level.
	 */
	public void testHandledByTopLevelEscalation() throws Exception {

		// Create the entries
		RoomEntry roomEntry = EntryUtil.createRoomEntry(null, null);
		DeskEntry deskEscalating = EntryUtil.createDeskEntry("DESK_ESCALATING",
				roomEntry);
		WorkEntry<Work> workEscalating = EntryUtil.createWorkEntry(
				"WORK_ESCALATING", Work.class, deskEscalating);
		TaskEntry<Work> taskEscalating = EntryUtil.createTaskEntry(
				"TASK_ESCALATING", workEscalating);
		FlowItemEscalationModel escalation = EntryUtil
				.createFlowItemEscalation(Exception.class, taskEscalating);
		ExternalEscalationModel deskExternalEscalation = EntryUtil
				.createExternalEscalation("DESK_ESCALATION", Exception.class,
						deskEscalating);
		new FlowItemEscalationToExternalEscalationModel(null, escalation,
				deskExternalEscalation).connect();
		SubRoomEscalationModel subRoomEscalation = EntryUtil
				.createSubRoomEscalation(deskExternalEscalation, deskEscalating);
		net.officefloor.model.room.ExternalEscalationModel roomExternalEscalation = EntryUtil
				.createExternalEscalation("ROOM_ESCALATION", Exception.class,
						roomEntry);
		new EscalationToExternalEscalationModel(null, subRoomEscalation,
				roomExternalEscalation).connect();

		// Create the flow escalation line
		FlowEscalationLine line = new FlowEscalationLine(escalation,
				taskEscalating);

		// Verify line
		this.verifyFlowEscalationLine(line, false, escalation,
				deskExternalEscalation, taskEscalating, workEscalating,
				deskEscalating, null, null, null, null, true);
	}

	/**
	 * Ensure handled by another {@link RoomModel}.
	 */
	public void testHandledByAnotherRoom() throws Exception {

		// Create the entries
		RoomEntry roomEntry = EntryUtil.createRoomEntry("TOP_LEVEL", null);

		// Escalating
		RoomEntry roomEscalating = EntryUtil.createRoomEntry("ROOM_ESCALATING",
				roomEntry);
		DeskEntry deskEscalating = EntryUtil.createDeskEntry("DESK_ESCALATING",
				roomEscalating);
		WorkEntry<Work> workEscalating = EntryUtil.createWorkEntry(
				"WORK_ESCALATING", Work.class, deskEscalating);
		TaskEntry<Work> taskEscalating = EntryUtil.createTaskEntry(
				"TASK_ESCALATING", workEscalating);
		FlowItemEscalationModel escalation = EntryUtil
				.createFlowItemEscalation(Exception.class, taskEscalating);
		ExternalEscalationModel externalEscalation = EntryUtil
				.createExternalEscalation("DESK_ESCALATION", Exception.class,
						deskEscalating);
		new FlowItemEscalationToExternalEscalationModel(null, escalation,
				externalEscalation).connect();
		SubRoomEscalationModel roomSubRoomEscalation = EntryUtil
				.createSubRoomEscalation(externalEscalation, deskEscalating);
		net.officefloor.model.room.ExternalEscalationModel roomExternalEscalation = EntryUtil
				.createExternalEscalation("ROOM_ESCALATION", Exception.class,
						roomEscalating);
		new EscalationToExternalEscalationModel(null, roomSubRoomEscalation,
				roomExternalEscalation).connect();
		SubRoomEscalationModel topLevelSubRoomEscalation = EntryUtil
				.createSubRoomEscalation(roomExternalEscalation, roomEscalating);

		// Handling
		RoomEntry roomHandling = EntryUtil.createRoomEntry("ROOM_HANDLING",
				roomEntry);
		DeskEntry deskHandling = EntryUtil.createDeskEntry("DESK_HANDLING",
				roomHandling);
		WorkEntry<Work> workHandling = EntryUtil.createWorkEntry(
				"WORK_HANDLING", Work.class, deskHandling);
		TaskEntry<Work> taskHandling = EntryUtil.createTaskEntry(
				"TASK_HANDLING", workHandling);
		SubRoomInputFlowModel roomInputFlowHandling = EntryUtil
				.createSubRoomInputFlow(taskHandling.getModel(), deskHandling);
		SubRoomInputFlowModel topLevelInputFlowHandling = EntryUtil
				.createSubRoomInputFlow(roomInputFlowHandling, deskHandling);

		// Link for handling
		new EscalationToInputFlowModel(null, null, topLevelSubRoomEscalation,
				topLevelInputFlowHandling).connect();

		// Create the flow escalation line
		FlowEscalationLine line = new FlowEscalationLine(escalation,
				taskEscalating);

		// Verify line
		this.verifyFlowEscalationLine(line, false, escalation,
				externalEscalation, taskEscalating, workEscalating,
				deskEscalating, taskHandling.getModel(), taskHandling,
				workHandling, deskHandling, false);
	}

	/**
	 * Verifies the {@link FlowEscalationLine}.
	 * 
	 * @param line
	 *            {@link FlowEscalationLine}.
	 * @param isSameWork
	 *            Flag indicating same {@link Work}.
	 * @param sourceFlowItemEscalation
	 *            Expected source {@link FlowItemEscalationModel}.
	 * @param sourceExternalEscalation
	 *            Expected source {@link ExternalEscalationModel}.
	 * @param sourceTask
	 *            Expected source {@link TaskEntry}.
	 * @param sourceWork
	 *            Expected source {@link WorkEntry}.
	 * @param sourceDesk
	 *            Expected source {@link DeskEntry}.
	 * @param targetFlowItem
	 *            Expected target {@link FlowItemModel}.
	 * @param targetTask
	 *            Expected target {@link TaskEntry}.
	 * @param targetWork
	 *            Expected target {@link WorkEntry}.
	 * @param targetDesk
	 *            Expected target {@link DeskEntry}.
	 * @param isTopLevelHandled
	 *            Flag indicating if handled by top level.
	 */
	private void verifyFlowEscalationLine(FlowEscalationLine line,
			boolean isSameWork,
			FlowItemEscalationModel sourceFlowItemEscalation,
			ExternalEscalationModel sourceExternalEscalation,
			TaskEntry<?> sourceTask, WorkEntry<?> sourceWork,
			DeskEntry sourceDesk, FlowItemModel targetFlowItem,
			TaskEntry<?> targetTask, WorkEntry<?> targetWork,
			DeskEntry targetDesk, boolean isTopLevelHandled) {
		assertEquals("Incorrect isSameWork", isSameWork, line.isSameWork());
		assertEquals("Incorrect source task escalation",
				sourceFlowItemEscalation, line.sourceFlowItemEscalation);
		assertEquals("Incorrect source external escalation",
				sourceExternalEscalation, line.sourceExternalEscalation);
		assertEquals("Incorrect source task", sourceTask, line.sourceTaskEntry);
		assertEquals("Incorrect source work", sourceWork, line.sourceWorkEntry);
		assertEquals("Incorrect source desk", sourceDesk, line.sourceDeskEntry);
		assertEquals("Incorrect target flow item", targetFlowItem,
				line.targetFlowItem);
		assertEquals("Incorrect target task", targetTask, line.targetTaskEntry);
		assertEquals("Incorrect target work", targetWork, line.targetWorkEntry);
		assertEquals("Incorrect target desk", targetDesk, line.targetDeskEntry);
		assertEquals("Incorrect handled by top level escalation",
				isTopLevelHandled, line.isHandledByTopLevelEscalation());
	}
}
