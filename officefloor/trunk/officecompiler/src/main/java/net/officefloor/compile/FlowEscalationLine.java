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
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.work.WorkModel;

/**
 * Contains the various items on the line from the
 * {@link FlowItemEscalationModel} through to its {@link FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowEscalationLine {

	/**
	 * Source {@link FlowItemEscalationModel}.
	 */
	public final FlowItemEscalationModel sourceFlowItemEscalation;

	/**
	 * Source {@link ExternalEscalationModel}. This will be <code>null</code> if
	 * target {@link FlowItemModel} is on the same {@link DeskModel}.
	 */
	public final ExternalEscalationModel sourceExternalEscalation;

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
	 * Target {@link FlowItemModel}. This will be <code>null</code> if handled
	 * by top level {@link Escalation}.
	 */
	public final FlowItemModel targetFlowItem;

	/**
	 * Initiate the line from the {@link FlowItemEscalationModel}.
	 * 
	 * @param flowItemEscalation
	 *            {@link FlowItemEscalationModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} containing the
	 *            {@link FlowItemEscalationModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public FlowEscalationLine(FlowItemEscalationModel flowItemEscalation,
			TaskEntry<?> taskEntry) throws Exception {

		// Store starting point
		this.sourceFlowItemEscalation = flowItemEscalation;
		this.sourceTaskEntry = taskEntry;

		// Obtain the source work and desk entry
		this.sourceWorkEntry = this.sourceTaskEntry.getWorkEntry();
		this.sourceDeskEntry = this.sourceWorkEntry.getDeskEntry();

		// Determine the where linked
		FlowItemEscalationToFlowItemModel flowItemLink = this.sourceFlowItemEscalation
				.getEscalationHandler();
		FlowItemEscalationToExternalEscalationModel externalEscalationLink = this.sourceFlowItemEscalation
				.getExternalEscalation();
		LinkedFlow target;
		if (flowItemLink != null) {

			// Handled by flow on same desk
			FlowItemModel targetFlowItem = flowItemLink.getHandler();
			target = FlowLineUtil.getLinkedFlow(targetFlowItem,
					this.sourceDeskEntry);

			// Not sourcing externally
			this.sourceExternalEscalation = null;

		} else if (externalEscalationLink != null) {

			// Handled by flow on another desk
			this.sourceExternalEscalation = externalEscalationLink
					.getExternalEscalation();
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
	 * Indicates if the next {@link FlowItemModel} is on the same
	 * {@link WorkModel}.
	 * 
	 * @return <code>true</code> if the next {@link FlowItemModel} is on the
	 *         same {@link WorkModel}.
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
