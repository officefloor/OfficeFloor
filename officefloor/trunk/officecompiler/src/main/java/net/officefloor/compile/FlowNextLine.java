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
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.model.work.WorkModel;

/**
 * Contains the various items on the line from the {@link FlowItemModel} through
 * to its next {@link FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowNextLine {

	/**
	 * Source {@link FlowItemModel}.
	 */
	public final FlowItemModel sourceFlowItem;

	/**
	 * Source {@link ExternalFlowModel}. This will be <code>null</code> if
	 * target {@link FlowItemModel} is on the same {@link DeskModel}.
	 */
	public final ExternalFlowModel sourceExternalFlow;

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
	 * Target {@link DeskEntry}. This will be <code>null</code> if no next
	 * {@link FlowItemModel}.
	 */
	public final DeskEntry targetDeskEntry;

	/**
	 * Target {@link WorkEntry}. This will be <code>null</code> if no next
	 * {@link FlowItemModel}.
	 */
	public final WorkEntry<?> targetWorkEntry;

	/**
	 * Target {@link TaskEntry}. This will be <code>null</code> if no next
	 * {@link FlowItemModel}.
	 */
	public final TaskEntry<?> targetTaskEntry;

	/**
	 * Target {@link FlowItemModel}. This will be <code>null</code> if no
	 * next {@link FlowItemModel}.
	 */
	public final FlowItemModel targetFlowItem;

	/**
	 * Initiate the line from the {@link FlowItemModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} containing the {@link FlowItemModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public FlowNextLine(FlowItemModel flowItem, TaskEntry<?> taskEntry)
			throws Exception {

		// Store starting point
		this.sourceFlowItem = flowItem;
		this.sourceTaskEntry = taskEntry;

		// Obtain the source work and desk entry
		this.sourceWorkEntry = this.sourceTaskEntry.getWorkEntry();
		this.sourceDeskEntry = this.sourceWorkEntry.getDeskEntry();

		// Determine if linked
		FlowItemToNextFlowItemModel flowItemLink = this.sourceFlowItem
				.getNextFlowItem();
		FlowItemToNextExternalFlowModel externalFlowLink = this.sourceFlowItem
				.getNextExternalFlow();
		LinkedFlow target = null;
		if (flowItemLink != null) {

			// Linked to flow on same desk
			FlowItemModel targetFlowItem = flowItemLink.getNextFlowItem();
			target = FlowLineUtil.getLinkedFlow(targetFlowItem,
					this.sourceDeskEntry);

			// Not sourcing externally
			this.sourceExternalFlow = null;

		} else if (externalFlowLink != null) {

			// Linked to flow on another desk
			this.sourceExternalFlow = externalFlowLink.getNextExternalFlow();
			target = FlowLineUtil.getLinkedFlow(this.sourceExternalFlow,
					this.sourceDeskEntry);

		} else {

			// No next flow
			this.sourceExternalFlow = null;
		}

		// Determine if a next flow item
		if (target != null) {
			// Has a next flow item
			this.targetDeskEntry = target.deskEntry;
			this.targetWorkEntry = target.workEntry;
			this.targetTaskEntry = target.taskEntry;
			this.targetFlowItem = target.flowItem;
		} else {
			// No next flow item
			this.targetDeskEntry = null;
			this.targetWorkEntry = null;
			this.targetTaskEntry = null;
			this.targetFlowItem = null;
		}
	}

	/**
	 * Indicates if the next {@link FlowItemModel} is on the same
	 * {@link WorkModel}.
	 * 
	 * @return <code>true</code> if the next {@link FlowItemModel} is on the
	 *         same {@link WorkModel}.
	 */
	public boolean isSameWork() {
		// No external flow so on same desk and work matches
		return ((this.sourceExternalFlow == null) && (this.sourceWorkEntry
				.getModel() == this.targetWorkEntry.getModel()));
	}
}
