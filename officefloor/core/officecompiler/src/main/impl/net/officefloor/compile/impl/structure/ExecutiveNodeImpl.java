/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.TeamOversightNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * {@link ExecutiveNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveNodeImpl implements ExecutiveNode {

	/**
	 * {@link PropertyList} to source the {@link Executive}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeFloorNode} containing this {@link ExecutiveNode}.
	 */
	private final OfficeFloorNode officeFloorNode;

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

		/**
		 * Class name of the {@link ExecutiveSource}.
		 */
		private final String executiveSourceClassName;

		/**
		 * Optional instantiated {@link ExecutiveSource}. May be <code>null</code>.
		 */
		private final ExecutiveSource executiveSource;

		/**
		 * Instantiate.
		 * 
		 * @param executiveSourceClassName Class name of the {@link ExecutiveSource}.
		 * @param executiveSource          Optional instantiated
		 *                                 {@link ExecutiveSource}. May be
		 *                                 <code>null</code>.
		 */
		public InitialisedState(String executiveSourceClassName, ExecutiveSource executiveSource) {
			this.executiveSourceClassName = executiveSourceClassName;
			this.executiveSource = executiveSource;
		}
	}

	/**
	 * {@link ExecutionStrategyNode} instances by their
	 * {@link OfficeFloorExecutionStrategy} name.
	 */
	private final Map<String, ExecutionStrategyNode> executionStrategies = new HashMap<>();

	/**
	 * {@link TeamOversightNode} instances by their {@link OfficeFloorTeamOversight}
	 * name.
	 */
	private final Map<String, TeamOversightNode> teamOversights = new HashMap<>();

	/**
	 * Initiate.
	 * 
	 * @param officeFloor {@link OfficeFloorNode} containing this
	 *                    {@link ExecutiveNode}.
	 * @param context     {@link NodeContext}.
	 */
	public ExecutiveNodeImpl(OfficeFloorNode officeFloor, NodeContext context) {
		this.officeFloorNode = officeFloor;
		this.context = context;

		// Create objects
		this.propertyList = this.context.createPropertyList();
	}

	/**
	 * Obtains the {@link ExecutiveSource}.
	 * 
	 * @return {@link ExecutiveSource} or <code>null</code> if issue obtaining with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	private ExecutiveSource getExecutiveSource() {

		// Load the executive source
		ExecutiveSource executiveSource = this.state.executiveSource;
		if (executiveSource == null) {

			// Obtain the executive source class
			Class<ExecutiveSource> executiveSourceClass = this.context
					.getExecutiveSourceClass(this.state.executiveSourceClassName, this);
			if (executiveSourceClass == null) {
				return null; // must have source class
			}

			// Instantiate the executive source
			executiveSource = CompileUtil.newInstance(executiveSourceClass, ExecutiveSource.class, this,
					this.context.getCompilerIssues());
		}

		// Return the executive source
		return executiveSource;
	}

	/*
	 * ==================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return "Executive";
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
		return this.officeFloorNode;
	}

	@Override
	public boolean isInitialised() {
		return this.state != null;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.executionStrategies, this.teamOversights);
	}

	/*
	 * ==================== ExecutiveNode ==========================
	 */
	@Override
	public void initialise(String executiveSourceClassName, ExecutiveSource executiveSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(executiveSourceClassName, executiveSource));
	}

	@Override
	public ExecutiveType loadExecutiveType() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public void buildExecutive(OfficeFloorBuilder builder, CompileContext compileContext) {

		// Obtain qualified name
		String qualifiedName = this.officeFloorNode.getQualifiedName(this.getNodeName());

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.context.overrideProperties(this, qualifiedName, this.propertyList);

		// Obtain the executive source
		ExecutiveSource executiveSource = this.getExecutiveSource();
		if (executiveSource == null) {
			return; // must obtain source
		}

		// Possibly register source as MBean
		compileContext.registerPossibleMBean(ExecutiveSource.class, this.getNodeName(), executiveSource);

		// Build the executive
		ExecutiveBuilder<?> executiveBuilder = builder.setExecutive(executiveSource);
		for (Property property : overriddenProperties) {
			executiveBuilder.addProperty(property.getName(), property.getValue());
		}
	}

	/*
	 * ================== OfficeFloorExecutive =====================
	 */

	@Override
	public String getOfficeFloorExecutiveName() {
		return this.getNodeName();
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public OfficeFloorExecutionStrategy getOfficeFloorExecutionStrategy(String executionStrategyName) {
		return NodeUtil.getInitialisedNode(executionStrategyName, this.executionStrategies, this.context,
				() -> this.context.createExecutionStrategyNode(executionStrategyName, this), (n) -> n.initialise());
	}

	@Override
	public OfficeFloorTeamOversight getOfficeFloorTeamOversight(String teamOversightName) {
		return NodeUtil.getInitialisedNode(teamOversightName, this.teamOversights, this.context,
				() -> this.context.createTeamOversightNode(teamOversightName, this), (n) -> n.initialise());
	}

}
