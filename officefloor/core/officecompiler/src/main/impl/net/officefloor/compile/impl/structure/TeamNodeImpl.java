/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
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
		 * Optional instantiated {@link TeamSource}. May be <code>null</code>.
		 */
		private final TeamSource teamSource;

		/**
		 * Instantiate.
		 * 
		 * @param teamSourceClassName Class name of the {@link TeamSource}.
		 * @param teamSource          Optional instantiated {@link TeamSource}. May be
		 *                            <code>null</code>.
		 */
		public InitialisedState(String teamSourceClassName, TeamSource teamSource) {
			this.teamSourceClassName = teamSourceClassName;
			this.teamSource = teamSource;
		}
	}

	/**
	 * {@link TypeQualification} instances for this {@link TeamNode}.
	 */
	private final List<TypeQualification> typeQualifications = new LinkedList<TypeQualification>();

	/**
	 * Initiate.
	 * 
	 * @param teamName    Name of this {@link OfficeFloorTeam}.
	 * @param officeFloor {@link OfficeFloorNode} containing this {@link TeamNode}.
	 * @param context     {@link NodeContext}.
	 */
	public TeamNodeImpl(String teamName, OfficeFloorNode officeFloor, NodeContext context) {
		this.teamName = teamName;
		this.officeFloorNode = officeFloor;
		this.context = context;

		// Create objects
		this.propertyList = this.context.createPropertyList();
	}

	/**
	 * Obtains the {@link TeamSource}.
	 * 
	 * @return {@link TeamSource} or <code>null</code> if issue obtaining with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	private TeamSource getTeamSource() {

		// Load the team source
		TeamSource teamSource = this.state.teamSource;
		if (teamSource == null) {

			// Obtain the team source class
			Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(this.state.teamSourceClassName, this);
			if (teamSourceClass == null) {
				return null; // must have source class
			}

			// Instantiate the team source
			teamSource = CompileUtil.newInstance(teamSourceClass, TeamSource.class, this,
					this.context.getCompilerIssues());
		}

		// Return the team source
		return teamSource;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	private PropertyList getProperties() {
		return this.context.overrideProperties(this, this.teamName, this.propertyList);
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
	public void initialise(String teamSourceClassName, TeamSource teamSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(teamSourceClassName, teamSource));
	}

	/*
	 * =============== TeamNode ======================================
	 */

	@Override
	public TeamType loadTeamType() {

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source
		TeamSource teamSource = this.getTeamSource();
		if (teamSource == null) {
			return null; // must have team source
		}

		// Load and return the team type
		return loader.loadTeamType(this.teamName, teamSource, this.getProperties());
	}

	@Override
	public OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(CompileContext compileContext) {

		// Ensure have the team name
		if (CompileUtil.isBlank(this.teamName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have the team source
		if (CompileUtil.isBlank(this.state.teamSourceClassName)) {
			this.context.getCompilerIssues().addIssue(this, "Null source for " + TYPE + " " + teamName);
			return null; // must have source
		}

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(this);

		// Obtain the team source
		TeamSource teamSource = this.getTeamSource();
		if (teamSource == null) {
			return null; // must have team source
		}

		// Load and return the team source type
		return loader.loadOfficeFloorTeamSourceType(this.teamName, teamSource, this.getProperties());
	}

	@Override
	public TypeQualification[] getTypeQualifications() {
		return this.typeQualifications.stream().toArray(TypeQualification[]::new);
	}

	@Override
	public void buildTeam(OfficeFloorBuilder builder, CompileContext compileContext) {

		// Obtain the team source
		TeamSource teamSource = this.getTeamSource();
		if (teamSource == null) {
			return; // must obtain team source
		}

		// Possibly register team source as MBean
		compileContext.registerPossibleMBean(TeamSource.class, this.teamName, teamSource);

		// Build the team
		TeamBuilder<?> teamBuilder = builder.addTeam(this.teamName, teamSource);
		for (Property property : this.getProperties()) {
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

	@Override
	public void addTypeQualification(String qualifier, String type) {
		this.typeQualifications.add(new TypeQualificationImpl(qualifier, type));
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
		return LinkUtil.linkTeamNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedTeamNode = link);
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}