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

import net.officefloor.compile.impl.section.OfficeSectionOutputTypeImpl;
import net.officefloor.compile.impl.section.SectionOutputTypeImpl;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.section.OfficeSectionOutputType;
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
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Argument type.
		 */
		private final String argumentType;

		/**
		 * Flag indicating if escalation only.
		 */
		private final boolean isEscalationOnly;

		/**
		 * Initialised state.
		 * 
		 * @param argumentType
		 *            Argument type.
		 * @param isEscalationOnly
		 *            Flag indicating if escalation only.
		 */
		public InitialisedState(String argumentType, boolean isEscalationOnly) {
			this.argumentType = argumentType;
			this.isEscalationOnly = isEscalationOnly;
		}
	}

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
	public SectionOutputNodeImpl(String outputName, SectionNode section,
			NodeContext context) {
		this.outputName = outputName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ================== Node =======================
	 */

	@Override
	public String getNodeName() {
		return this.outputName;
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
		return this.section;
	}

	/*
	 * ================== SectionOutputNode =======================
	 */

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public SectionOutputNode initialise(String argumentType,
			boolean isEscalationOnly) {

		// Ensure not already initialise
		if (this.isInitialised()) {
			throw new IllegalStateException("SectionOutputNode "
					+ this.outputName + " already initialised");
		}

		// Initialise
		this.state = new InitialisedState(argumentType, isEscalationOnly);
		return this;
	}

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public SectionOutputType loadSectionOutputType() {
		return new SectionOutputTypeImpl(this.outputName,
				this.state.argumentType, this.state.isEscalationOnly);
	}

	@Override
	public OfficeSectionOutputType loadOfficeSectionOutputType() {
		return new OfficeSectionOutputTypeImpl(this.outputName,
				this.state.argumentType, this.state.isEscalationOnly);
	}

	/*
	 * ================ SectionOutput =========================
	 */

	@Override
	public String getSectionOutputName() {
		return this.outputName;
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