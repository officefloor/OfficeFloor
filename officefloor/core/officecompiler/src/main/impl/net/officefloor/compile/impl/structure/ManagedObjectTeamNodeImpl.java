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
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link TaskTeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTeamNodeImpl implements ManagedObjectTeamNode {

	/**
	 * Name of this {@link OfficeTeam}.
	 */
	private final String teamName;

	/**
	 * {@link ManagedObjectSourceNode} containing this
	 * {@link ManagedObjectTeamNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeTeam}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSourceNode} containing this
	 *            {@link ManagedObjectTeamNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectTeamNodeImpl(String teamName,
			ManagedObjectSourceNode managedObjectSource, NodeContext context) {
		this.teamName = teamName;
		this.managedObjectSourceNode = managedObjectSource;
		this.context = context;
	}

	/*
	 * ================== Node =========================
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
		return this.managedObjectSourceNode;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState());
	}

	/*
	 * ================ ManagedObjectTeamNode =======================
	 */

	@Override
	public OfficeSectionManagedObjectTeamType loadOfficeSectionManagedObjectTeamType(
			TypeContext typeContext) {
		// TODO implement
		// ManagedObjectTeamNode.loadOfficeSectionManagedObjectTeamType
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectTeamNode.loadOfficeSectionManagedObjectTeamType");

	}

	/*
	 * ================== ManagedObjectTeam =========================
	 */

	@Override
	public String getManagedObjectTeamName() {
		return this.teamName;
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
		return LinkUtil.linkTeamNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedTeamNode = link);
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}