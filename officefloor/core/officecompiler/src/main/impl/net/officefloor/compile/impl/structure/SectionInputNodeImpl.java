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

import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.impl.section.OfficeSectionInputTypeImpl;
import net.officefloor.compile.impl.section.SectionInputTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;

/**
 * {@link SectionInputNode} node.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInputNodeImpl implements SectionInputNode {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * {@link SectionNode} containing this {@link SectionInputNode}.
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
		 * Parameter type.
		 */
		private String parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param parameterType
		 *            Parameter type.
		 */
		public InitialisedState(String parameterType) {
			this.parameterType = parameterType;
		}
	}

	/**
	 * {@link ExternalServiceProxyFunctionManager}.
	 */
	private ExternalServiceProxyFunctionManager externalFunctionManager = null;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} (which is the name of the
	 *            {@link SectionInputType}).
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionInputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionInputNodeImpl(String inputName, SectionNode section, NodeContext context) {
		this.inputName = inputName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.inputName;
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
	public void initialise(String parameterType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(parameterType));
	}

	/*
	 * ===================== SectionInputNode ===========================
	 */

	@Override
	public void loadExternalServicing(Office office) throws UnknownFunctionException {

		// Determine if externally triggered
		if (this.externalFunctionManager == null) {
			return; // not externally triggered
		}

		// Obtain the function node servicing this section input
		ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(this, ManagedFunctionNode.class,
				new FailCompilerIssues());
		if (functionNode == null) {
			// Compiled, so this should not occur
			return; // must have function
		}

		// Obtain the corresponding function manager
		String functionName = functionNode.getQualifiedFunctionName();
		FunctionManager functionManager = office.getFunctionManager(functionName);

		// Load the function to the proxy
		this.externalFunctionManager.delegate = functionManager;
	}

	@Override
	public SectionInputType loadSectionInputType(CompileContext compileContext) {

		// Ensure have input name
		if (CompileUtil.isBlank(this.inputName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have names for inputs
		}

		// Create and return type
		return new SectionInputTypeImpl(this.inputName, this.state.parameterType);
	}

	@Override
	public OfficeSectionInputType loadOfficeSectionInputType(CompileContext compileContext) {
		return new OfficeSectionInputTypeImpl(this.inputName, this.state.parameterType);
	}

	/*
	 * ================= SectionInput =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	/*
	 * =================== SubSectionInput ========================
	 */

	@Override
	public String getSubSectionInputName() {
		return this.inputName;
	}

	/*
	 * ===================== OfficeSectionInput =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	/*
	 * ====================== DeployedOfficeInput ====================
	 */

	@Override
	public String getDeployedOfficeInputName() {
		return this.inputName;
	}

	@Override
	public DeployedOffice getDeployedOffice() {
		return this.section.getOfficeNode();
	}

	@Override
	public FunctionManager getFunctionManager() {

		// Lazy create the function manager
		if (this.externalFunctionManager == null) {
			this.externalFunctionManager = new ExternalServiceProxyFunctionManager();
		}

		// Return the external function manager
		return this.externalFunctionManager;
	}

	/*
	 * =================== LinkFlowNode ============================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

	/**
	 * Proxy {@link FunctionManager} for external service handling.
	 */
	private static class ExternalServiceProxyFunctionManager implements FunctionManager {

		/**
		 * Delegate {@link FunctionManager}.
		 */
		private FunctionManager delegate = null;

		@Override
		public Object getDifferentiator() {
			return this.delegate.getDifferentiator();
		}

		@Override
		public Class<?> getParameterType() {
			return this.delegate.getParameterType();
		}

		@Override
		public void invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException {
			this.delegate.invokeProcess(parameter, callback);
		}
	}

}