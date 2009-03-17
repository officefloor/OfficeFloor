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
import net.officefloor.compile.desk.DeskLoader;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.work.WorkModel;

/**
 * Contains the various items on the line from the {@link FlowItemOutputModel}
 * through to its {@link FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowOutputLine {

	/**
	 * Source {@link FlowItemOutputModel}.
	 */
	public final FlowItemOutputModel sourceFlowItemOutput;

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
	 * {@link FlowInstigationStrategyEnum}.
	 */
	public final FlowInstigationStrategyEnum flowInstigationStrategy;

	/**
	 * Target {@link DeskEntry}.
	 */
	public final DeskEntry targetDeskEntry;

	/**
	 * Target {@link WorkEntry}.
	 */
	public final WorkEntry<?> targetWorkEntry;

	/**
	 * Target {@link TaskEntry}.
	 */
	public final TaskEntry<?> targetTaskEntry;

	/**
	 * Target {@link FlowItemModel}.
	 */
	public final FlowItemModel targetFlowItem;

	/**
	 * Initiate the line from the {@link FlowItemOutputModel}.
	 * 
	 * @param flowItemOutput
	 *            {@link FlowItemOutputModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} containing the {@link FlowItemOutputModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public FlowOutputLine(FlowItemOutputModel flowItemOutput,
			TaskEntry<?> taskEntry) throws Exception {

		// Store starting point
		this.sourceFlowItemOutput = flowItemOutput;
		this.sourceTaskEntry = taskEntry;

		// Obtain the source work and desk entry
		this.sourceWorkEntry = this.sourceTaskEntry.getWorkEntry();
		this.sourceDeskEntry = this.sourceWorkEntry.getDeskEntry();

		// Determine the where linked
		FlowItemOutputToFlowItemModel flowItemLink = this.sourceFlowItemOutput
				.getFlowItem();
		FlowItemOutputToExternalFlowModel externalFlowLink = this.sourceFlowItemOutput
				.getExternalFlow();
		LinkedFlow target;
		String instigationType;
		if (flowItemLink != null) {

			// Linked to flow on same desk
			FlowItemModel targetFlowItem = flowItemLink.getFlowItem();
			target = FlowLineUtil.getLinkedFlow(targetFlowItem,
					this.sourceDeskEntry);
			instigationType = flowItemLink.getLinkType();

			// Not sourcing externally
			this.sourceExternalFlow = null;

		} else if (externalFlowLink != null) {

			// Linked to flow on another desk
			this.sourceExternalFlow = externalFlowLink.getExternalFlow();
			target = FlowLineUtil.getLinkedFlow(this.sourceExternalFlow,
					this.sourceDeskEntry);
			instigationType = externalFlowLink.getLinkType();

		} else {
			// Flow output not linked
			throw new Exception("Flow item output "
					+ this.sourceFlowItemOutput.getId() + " on flow item "
					+ this.sourceTaskEntry.getId() + " not linked to a flow");
		}

		// Indicate the instigation strategy
		this.flowInstigationStrategy = DeskLoader
				.getFlowInstigationStrategyEnum(instigationType);

		// Specify the target details
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
		// No external flow so on same desk and work matches
		return ((this.sourceExternalFlow == null) && (this.sourceWorkEntry
				.getModel() == this.targetWorkEntry.getModel()));
	}

}
