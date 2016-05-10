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
import net.officefloor.compile.internal.structure.LinkSynchronousNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.frame.api.manage.Office;

/**
 * Implementation of the {@link OfficeFloorInputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputNodeImpl implements OfficeInputNode, OfficeInputType {

	/**
	 * Name of this {@link OfficeFloorInput}.
	 */
	private final String name;

	/**
	 * Parameter type of this {@link OfficeFloorInput}.
	 */
	private final String parameterType;

	/**
	 * Location of the {@link Office} containing this {@link OfficeInputNode}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link LinkSynchronousNode} being the {@link OfficeOutputNode}.
	 */
	private OfficeOutputNode linkedSynchronousNode = null;

	/**
	 * {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name of {@link OfficeFloorInput}.
	 * @param parameterType
	 *            Parameter type of {@link OfficeFloorInput}.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeInputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeInputNodeImpl(String name, String parameterType,
			String officeLocation, NodeContext context) {
		this.name = name;
		this.parameterType = parameterType;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/*
	 * ======================= OfficeInputNode ===========================
	 */

	@Override
	public OfficeInputType getOfficeInputType() {
		return this;
	}

	/*
	 * ===================== LinkSynchronousNode =========================
	 */

	@Override
	public boolean linkSynchronousNode(LinkSynchronousNode node) {

		// Ensure not already linked
		if (this.linkedSynchronousNode != null) {
			this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
					this.officeLocation, null, null,
					"Input " + this.name + " linked more than once");
			return false; // already linked
		}

		// Ensure is an output
		if (!(node instanceof OfficeOutputNode)) {
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE,
					this.officeLocation,
					null,
					null,
					"Input " + this.name
							+ " may only be synchronously linked to an "
							+ OfficeOutputNode.class.getSimpleName());
			return false; // already linked
		}

		// Link
		this.linkedSynchronousNode = (OfficeOutputNode) node;
		return true;
	}

	@Override
	public LinkSynchronousNode getLinkedSynchronousNode() {
		return this.linkedSynchronousNode;
	}

	/*
	 * ======================== LinkFlowNode =============================
	 */

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
					this.officeLocation, null, null,
					"Input " + this.name + " linked more than once");
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

	/*
	 * ========================= OfficeInput =============================
	 */

	@Override
	public String getOfficeInputName() {
		return this.name;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	/*
	 * ======================= OfficeInputType ===========================
	 */

	@Override
	public OfficeOutputType getResponseOfficeOutputType() {
		return (this.linkedSynchronousNode == null ? null
				: this.linkedSynchronousNode.getOfficeOutputType());
	}

}