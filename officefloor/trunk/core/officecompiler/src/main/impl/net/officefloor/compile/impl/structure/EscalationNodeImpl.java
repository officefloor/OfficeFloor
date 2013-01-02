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

import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link EscalationNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationNodeImpl implements EscalationNode {

	/**
	 * {@link Escalation} type.
	 */
	private final String escalationType;

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @param officeLocation
	 *            {@link Office} location.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public EscalationNodeImpl(String escalationType, String officeLocation,
			NodeContext context) {
		this.escalationType = escalationType;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/*
	 * ======================== OfficeEscalation =======================
	 */

	@Override
	public String getOfficeEscalationType() {
		return this.escalationType;
	}

	/*
	 * ======================== LinkFlowNode =======================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			// Office Escalation linked
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE,
					this.officeLocation,
					null,
					null,
					"Office escalation " + this.escalationType
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