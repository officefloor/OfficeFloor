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
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.section.SectionOutputType;

/**
 * {@link SectionOutputNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionOutputNodeImpl implements SectionOutputNode {

	/**
	 * Name of the {@link SectionOutputType}.
	 */
	private final String outputName;

	/**
	 * {@link SectionNode} containing this {@link SectionOutputNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if this {@link SectionOutputType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Argument type.
	 */
	private String argumentType;

	/**
	 * Flag indicating if escalation only.
	 */
	private boolean isEscalationOnly;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionOutputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionOutputNodeImpl(String outputName, String sectionLocation,
			SectionNode section, NodeContext context) {
		this.outputName = outputName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ================== Node =======================
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
	 * ================== SectionOutputNode =======================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public SectionOutputNode initialise(String argumentType,
			boolean isEscalationOnly) {
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
		this.isInitialised = true;
		return this;
	}

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	/*
	 * ================ SectionOutputType =========================
	 */

	@Override
	public String getSectionOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	@Override
	public boolean isEscalationOnly() {
		return this.isEscalationOnly;
	}

	/*
	 * ================ SubSectionOutput ===========================
	 */

	@Override
	public String getSubSectionOutputName() {
		return this.outputName;
	}

	/*
	 * =================== OfficeSectionOutput ======================
	 */

	@Override
	public String getOfficeSectionOutputName() {
		return this.outputName;
	}

	/*
	 * ==================== LinkFlowNode ===========================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			this.context.getCompilerIssues().addIssue(
					this,
					"Section output " + this.outputName
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