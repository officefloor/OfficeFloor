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

import net.officefloor.compile.FlowLineUtil.LinkedFlow;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskModel;

/**
 * Contains the various items on the line from the {@link TaskEscalationModel}
 * through to its {@link TaskModel}.
 * 
 * @author Daniel
 */
public class FlowEscalationLine {

	/**
	 * Source {@link TaskEscalationModel}.
	 */
	public final TaskEscalationModel sourceFlowItemEscalation;

	/**
	 * Source {@link ExternalFlowModel}. This will be <code>null</code> if
	 * target {@link TaskModel} is on the same {@link DeskModel}.
	 */
	public final ExternalFlowModel sourceExternalEscalation;

	/**
	 * Source {@link TaskEntry}.
	 */
	public final TaskEntry<?> sourceTaskEntry;

	/**
	 * Source {@link WorkEntry}.
	 */
	public final WorkEntry<?> sourceWorkEntry;

	/**
	 * Source {@link DeskEntry}.
	 */
	public final DeskEntry sourceDeskEntry;

	/**
	 * Target {@link DeskEntry}. This will be <code>null</code> if handled by
	 * top level {@link Escalation}.
	 */
	public final DeskEntry targetDeskEntry;

	/**
	 * Target {@link WorkEntry}. This will be <code>null</code> if handled by
	 * top level {@link Escalation}.
	 */
	public final WorkEntry<?> targetWorkEntry;

	/**
	 * Target {@link TaskEntry}. This will be <code>null</code> if handled by
	 * top level {@link Escalation}.
	 */
	public final TaskEntry<?> targetTaskEntry;

	/**
	 * Target {@link TaskModel}. This will be <code>null</code> if handled by
	 * top level {@link Escalation}.
	 */
	public final TaskModel targetFlowItem;

	/**
	 * Initiate the line from the {@link TaskEscalationModel}.
	 * 
	 * @param flowItemEscalation
	 *            {@link TaskEscalationModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} containing the {@link TaskEscalationModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public FlowEscalationLine(TaskEscalationModel flowItemEscalation,
			TaskEntry<?> taskEntry) throws Exception {

		// Store starting point
		this.sourceFlowItemEscalation = flowItemEscalation;
		this.sourceTaskEntry = taskEntry;

		// Obtain the source work and desk entry
		this.sourceWorkEntry = this.sourceTaskEntry.getWorkEntry();
		this.sourceDeskEntry = this.sourceWorkEntry.getDeskEntry();

		// Determine the where linked
		TaskEscalationToTaskModel flowItemLink = this.sourceFlowItemEscalation
				.getTask();
		TaskEscalationToExternalFlowModel externalEscalationLink = this.sourceFlowItemEscalation
				.getExternalFlow();
		LinkedFlow target;
		if (flowItemLink != null) {

			// Handled by flow on same desk
			TaskModel targetFlowItem = flowItemLink.getTask();
			target = FlowLineUtil.getLinkedFlow(targetFlowItem,
					this.sourceDeskEntry);

			// Not sourcing externally
			this.sourceExternalEscalation = null;

		} else if (externalEscalationLink != null) {

			// Handled by flow on another desk
			this.sourceExternalEscalation = externalEscalationLink
					.getExternalFlow();
			target = FlowLineUtil.getLinkedFlow(this.sourceExternalEscalation,
					this.sourceDeskEntry);

		} else {
			// Flow escalation not linked
			throw new Exception("Flow item escalation "
					+ this.sourceFlowItemEscalation.getEscalationType()
					+ " on flow item " + this.sourceTaskEntry.getId()
					+ " not linked to a flow");
		}

		// Specify the target details (will be null if top level)
		this.targetDeskEntry = target.deskEntry;
		this.targetWorkEntry = target.workEntry;
		this.targetTaskEntry = target.taskEntry;
		this.targetFlowItem = target.flowItem;
	}

	/**
	 * Indicates if the next {@link TaskModel} is on the same {@link WorkType}.
	 * 
	 * @return <code>true</code> if the next {@link TaskModel} is on the same
	 *         {@link WorkType}.
	 */
	public boolean isSameWork() {
		// No external escalation so on same desk and work matches
		return ((this.sourceExternalEscalation == null) && (this.sourceWorkEntry
				.getModel() == this.targetWorkEntry.getModel()));
	}

	/**
	 * Indicates if the {@link Escalation} is handled by the top level
	 * {@link Escalation}.
	 * 
	 * @return <code>true</code> if the {@link Escalation} is handled by the top
	 *         level {@link Escalation}.
	 */
	public boolean isHandledByTopLevelEscalation() {
		return (this.targetFlowItem == null);
	}
}
