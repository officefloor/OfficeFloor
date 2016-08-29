/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link TaskFlowNode} implementation.
 * 
 * @author Daniel Sagenschneider
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
	 * {@link SectionNode} containing this {@link TaskFlow}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link TaskFlow}.
	 * @param isEscalation
	 *            Indicates if this {@link TaskFlow} is for a
	 *            {@link TaskEscalationType}.
	 * @param section
	 *            {@link SectionNode} containing this {@link TaskFlow}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskFlowNodeImpl(String flowName, boolean isEscalation,
			SectionNode section, NodeContext context) {
		this.flowName = flowName;
		this.isEscalation = isEscalation;
		this.section = section;
		this.context = context;

		// If escalation, then flow instigation strategy always sequential
		this.instigationStrategy = FlowInstigationStrategyEnum.SEQUENTIAL;
	}

	/*
	 * ================== Node ======================================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

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
			this.context.getCompilerIssues().addIssue(this,
					"Task flow " + this.flowName + " linked more than once");
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