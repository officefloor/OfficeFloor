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
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

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
	 * Class name of the {@link TeamSource}.
	 */
	private final String teamSourceClassName;

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
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeFloorTeam}.
	 * @param teamSourceClassName
	 *            Class name of the {@link TeamSource}.
	 * @param officeFloor
	 *            {@link OfficeFloorNode} containing this {@link TeamNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TeamNodeImpl(String teamName, String teamSourceClassName,
			OfficeFloorNode officeFloor, NodeContext context) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
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
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement TeamNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement TeamNode.initialise");

	}

	/*
	 * =============== TeamNode ======================================
	 */

	@Override
	public boolean hasTeamSource() {
		return !CompileUtil.isBlank(this.teamSourceClassName);
	}

	@Override
	public TeamType loadTeamType() {

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(
				this.teamSourceClassName, this);
		if (teamSourceClass == null) {
			return null; // must have source class
		}

		// Load and return the team type
		return loader.loadTeamType(this.teamName, teamSourceClass,
				this.propertyList);
	}

	@Override
	public OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType() {

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(
				this.teamSourceClassName, this);
		if (teamSourceClass == null) {
			return null; // must have source class
		}

		// Load and return the team source type
		return loader.loadOfficeFloorTeamSourceType(this.teamName,
				teamSourceClass, this.propertyList);
	}

	@Override
	public void buildTeam(OfficeFloorBuilder builder) {

		// Obtain the team source class
		Class<? extends TeamSource> teamSourceClass = this.context
				.getTeamSourceClass(this.teamSourceClassName, this);
		if (teamSourceClass == null) {
			return; // must obtain team source class
		}

		// Build the team
		TeamBuilder<?> teamBuilder = builder.addTeam(this.teamName,
				teamSourceClass);
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