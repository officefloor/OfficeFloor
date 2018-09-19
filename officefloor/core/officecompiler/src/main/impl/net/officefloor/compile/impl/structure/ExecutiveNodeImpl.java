/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
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
		return new Node[0];
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

	}

	@Override
	public OfficeFloorExecutionStrategy getOfficeFloorExecutionStrategy(String executionStrategyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OfficeFloorTeamOversight getOfficeFloorTeamOversight(String teamOversightName) {
		// TODO Auto-generated method stub
		return null;
	}

}