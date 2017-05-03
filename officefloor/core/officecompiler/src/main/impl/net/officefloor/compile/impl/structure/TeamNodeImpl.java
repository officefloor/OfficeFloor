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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * {@link TeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamNodeImpl implements TeamNode {

	/**
	 * Name of this {@link OfficeFloorTeam}.
	 */
	private final String teamName;

	/**
	 * {@link PropertyList} to source the {@link Team}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeFloorNode} containing this {@link TeamNode}.
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
		 * Class name of the {@link TeamSource}.
		 */
		private final String teamSourceClassName;

		/**
		 * Instantiate.
		 * 
		 * @param teamSourceClassName
		 *            Class name of the {@link TeamSource}.
		 */
		public InitialisedState(String teamSourceClassName) {
			this.teamSourceClassName = teamSourceClassName;
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeFloorTeam}.
	 * @param officeFloor
	 *            {@link OfficeFloorNode} containing this {@link TeamNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TeamNodeImpl(String teamName, OfficeFloorNode officeFloor, NodeContext context) {
		this.teamName = teamName;
		this.officeFloorNode = officeFloor;
		this.context = context;

		// Create objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * ========================= Node ======================================
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
		return this.officeFloorNode;
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
	public void initialise(String teamSourceClassName) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(teamSourceClassName));
	}

	/*
	 * =============== TeamNode ======================================
	 */

	@Override
	public boolean hasTeamSource() {
		return !CompileUtil.isBlank(this.state.teamSourceClassName);
	}

	@Override
	public TeamType loadTeamType() {

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(this.state.teamSourceClassName, this);
		if (teamSourceClass == null) {
			return null; // must have source class
		}

		// Load and return the team type
		return loader.loadTeamType(this.teamName, teamSourceClass, this.propertyList);
	}

	@Override
	public OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(TypeContext typeContext) {

		// Ensure have the team name
		if (CompileUtil.isBlank(this.teamName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have the team source
		if (!this.hasTeamSource()) {
			this.context.getCompilerIssues().addIssue(this, "Null source for " + TYPE + " " + teamName);
			return null; // must have source
		}

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(this.state.teamSourceClassName, this);
		if (teamSourceClass == null) {
			return null; // must have source class
		}

		// Load and return the team source type
		return loader.loadOfficeFloorTeamSourceType(this.teamName, teamSourceClass, this.propertyList);
	}

	@Override
	public void buildTeam(OfficeFloorBuilder builder) {

		// Obtain the team source class
		Class<? extends TeamSource> teamSourceClass = this.context.getTeamSourceClass(this.state.teamSourceClassName,
				this);
		if (teamSourceClass == null) {
			return; // must obtain team source class
		}

		// Build the team
		TeamBuilder<?> teamBuilder = builder.addTeam(this.teamName, teamSourceClass);
		for (Property property : this.propertyList) {
			teamBuilder.addProperty(property.getName(), property.getValue());
		}
	}

	/*
	 * ============= OfficeFloorTeam ====================================
	 */

	@Override
	public String getOfficeFloorTeamName() {
		return this.teamName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	/*
	 * =============== LinkTeamNode ===================================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {
		// Link
		this.linkedTeamNode = node;
		return true;
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}