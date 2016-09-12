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

import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link TaskTeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskTeamNodeImpl implements TaskTeamNode {

	/**
	 * Name of this {@link OfficeTeam}.
	 */
	private final String teamName;

	/**
	 * {@link TaskNode} containing this {@link TaskTeamNode}.
	 */
	private final TaskNode taskNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeTeam}.
	 * @param taskNode
	 *            {@link TaskNode} containing this {@link TaskTeamNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskTeamNodeImpl(String teamName, TaskNode taskNode,
			NodeContext context) {
		this.teamName = teamName;
		this.taskNode = taskNode;
		this.context = context;
	}

	/*
	 * ================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.teamName;
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
		return this.taskNode;
	}

	/*
	 * ================== LinkTeamNode ============================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {

		// Ensure not already linked
		if (this.linkedTeamNode != null) {
			this.context.getCompilerIssues().addIssue(this,
					this.teamName + " already assigned");
		}

		// Link
		this.linkedTeamNode = node;
		return true;
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}