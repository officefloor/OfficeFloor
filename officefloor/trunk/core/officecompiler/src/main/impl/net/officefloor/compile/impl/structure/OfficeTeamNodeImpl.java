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

import net.officefloor.compile.impl.office.OfficeTeamTypeImpl;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link OfficeTeamNode} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTeamNodeImpl implements OfficeTeamNode {

	/**
	 * {@link OfficeTeam} name.
	 */
	private final String teamName;

	/**
	 * Parent {@link OfficeNode}.
	 */
	private final OfficeNode office;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Instantiate.
	 * 
	 * @param teamName
	 *            {@link OfficeTeam} name.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeTeamNodeImpl(String teamName, OfficeNode office,
			NodeContext context) {
		this.teamName = teamName;
		this.office = office;
		this.context = context;
	}

	/*
	 * ====================== Node ===========================
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
		return this.office;
	}

	/*
	 * ====================== OfficeTeam ===========================
	 */

	@Override
	public String getOfficeTeamName() {
		// TODO implement OfficeTeam.getOfficeTeamName
		throw new UnsupportedOperationException(
				"TODO implement OfficeTeam.getOfficeTeamName");

	}

	/*
	 * ==================== OfficeTeamNode =========================
	 */

	@Override
	public OfficeTeamType loadOfficeTeamType(TypeContext typeContext) {
		return new OfficeTeamTypeImpl(this.teamName);
	}

	/*
	 * ====================== LinkTeamNode ===========================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {

		// Ensure not already linked
		if (this.linkedTeamNode != null) {
			// Deployed office team
			this.context.getCompilerIssues().addIssue(this,
					this.teamName + " already assigned");
			return false; // already linked
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