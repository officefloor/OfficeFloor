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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link TaskFlowNode} implementation.
 * 
 * @author Daniel
 */
public class TaskFlowNodeImpl implements TaskFlowNode {

	/**
	 * Name of this {@link TaskFlow}.
	 */
	private final String flowName;

	/**
	 * Indicates if this {@link TaskFlow} is for a {@link TaskEscalationType}.
	 */
	private final boolean isEscalation;

	/**
	 * Location of the {@link OfficeSection} containing this {@link TaskFlow}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link TaskFlow}.
	 * @param isEscalation
	 *            Indicates if this {@link TaskFlow} is for a
	 *            {@link TaskEscalationType}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link TaskFlow}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public TaskFlowNodeImpl(String flowName, boolean isEscalation,
			String sectionLocation, CompilerIssues issues) {
		this.flowName = flowName;
		this.isEscalation = isEscalation;
		this.sectionLocation = sectionLocation;
		this.issues = issues;

		// If escalation, then flow instigation strategy always sequential
		this.instigationStrategy = FlowInstigationStrategyEnum.SEQUENTIAL;
	}

	/*
	 * ================== TaskFlow ======================================
	 */

	@Override
	public String getTaskFlowName() {
		return this.flowName;
	}

	/*
	 * ================== TaskFlowNode ==================================
	 */

	/**
	 * {@link FlowInstigationStrategyEnum} for this {@link TaskFlow}.
	 */
	private FlowInstigationStrategyEnum instigationStrategy;

	@Override
	public void setFlowInstigationStrategy(
			FlowInstigationStrategyEnum instigationStrategy) {
		// May only specify if not escalation
		if (!this.isEscalation) {
			this.instigationStrategy = instigationStrategy;
		}
	}

	@Override
	public FlowInstigationStrategyEnum getFlowInstigationStrategy() {
		return this.instigationStrategy;
	}

	/*
	 * =================== LinkFlowNode ==================================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			this.issues.addIssue(LocationType.SECTION, this.sectionLocation,
					null, null, "Task flow " + this.flowName
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedFlowNode = node;
		return true;
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}