/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.section.OfficeSectionOutputTypeImpl;
import net.officefloor.compile.impl.section.SectionOutputTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.OfficeSection;

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
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

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
	public SectionOutputNodeImpl(String outputName, SectionNode section, NodeContext context) {
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

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String argumentType, boolean isEscalationOnly) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(argumentType, isEscalationOnly));
	}

	/*
	 * ================== SectionOutputNode =======================
	 */

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public SectionOutputType loadSectionOutputType(CompileContext compileContext) {

		// Ensure have output name
		if (CompileUtil.isBlank(this.outputName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have names for outputs
		}

		// Create and return type
		return new SectionOutputTypeImpl(this.outputName, this.state.argumentType, this.state.isEscalationOnly,
				this.annotations.toArray(new Object[this.annotations.size()]));
	}

	@Override
	public OfficeSectionOutputType loadOfficeSectionOutputType(CompileContext compileContext) {
		return new OfficeSectionOutputTypeImpl(this.outputName, this.state.argumentType, this.state.isEscalationOnly,
				this.annotations.toArray(new Object[this.annotations.size()]));
	}

	/*
	 * ================ SectionOutput =========================
	 */

	@Override
	public String getSectionOutputName() {
		return this.outputName;
	}

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
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
	public OfficeSection getOfficeSection() {
		return this.section;
	}

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
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}
