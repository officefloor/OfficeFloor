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

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
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
	 * Parent {@link TaskNode}.
	 */
	private final TaskNode task;

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
	 * @param task
	 *            Parent {@link TaskNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskFlowNodeImpl(String flowName, boolean isEscalation,
			TaskNode task, NodeContext context) {
		this.flowName = flowName;
		this.isEscalation = isEscalation;
		this.task = task;
		this.context = context;

		// If escalation, then flow instigation strategy always sequential
		this.instigationStrategy = FlowInstigationStrategyEnum.SEQUENTIAL;
	}

	/*
	 * ================== Node ======================================
	 */

	@Override
	public String getNodeName() {
		return this.flowName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.task;
	}

	@Override
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement TaskFlowNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement TaskFlowNode.initialise");

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
		return LinkUtil.linkFlowNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}