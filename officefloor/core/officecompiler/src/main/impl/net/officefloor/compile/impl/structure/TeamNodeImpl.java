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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.TeamVisitor;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
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
import net.officefloor.frame.api.executive.TeamOversight;
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
	 * Size of the {@link Team}.
	 */
	private Integer teamSize = null;

	/**
	 * Indicates if request no {@link TeamOversight}.
	 */
	private boolean isRequestNoTeamOversight = false;

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
	 * Convenience method to add a {@link CompilerIssue}.
	 * 
	 * @param issueDescription Description for the {@link CompilerIssue}.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(this, issueDescription);
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
	public boolean sourceTeam(TeamVisitor visitor, CompileContext compileContext) {

		// Load the team type
		TeamType teamType = compileContext.getOrLoadTeamType(this);
		if (teamType == null) {
			return false;
		}

		// Determine if require team size
		if (teamType.isRequireTeamSize()) {

			// Ensure have team size
			if (this.teamSize == null) {
				this.addIssue("Team size must be specified for team '" + this.teamName + "'");
				return false; // can not carry on
			}

			// Ensure valid team size
			if (this.teamSize < 1) {
				this.addIssue(
						"Invalid size (" + this.teamSize + ") for team '" + this.teamName + "'.  Must be 1 or more.");
				return false; // can not carry on
			}
		}

		// Visit this team
		if (visitor != null) {
			try {
				visitor.visit(teamType, this, compileContext);
			} catch (CompileError error) {
				// Issue should already be provided
				return false;
			}
		}

		// Successfully sourced
		return true;
	}

	@Override
	public TeamType loadTeamType() {

		// Obtain the loader

		// Obtain the team source
		TeamSource teamSource = this.getTeamSource();
		if (teamSource == null) {
			return null; // must have team source
		}

		// Load and return the team type
		TeamLoader loader = this.context.getTeamLoader(this);
		return loader.loadTeamType(this.teamName, teamSource, this.propertyList);
	}

	@Override
	public OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(CompileContext compileContext) {

		// Ensure have the team name
		if (CompileUtil.isBlank(this.teamName)) {
			this.addIssue("Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have the team source
		if (CompileUtil.isBlank(this.state.teamSourceClassName)) {
			this.addIssue("Null source for " + TYPE + " " + teamName);
			return null; // must have source
		}

		// Obtain the team source
		TeamSource teamSource = this.getTeamSource();
		if (teamSource == null) {
			return null; // must have team source
		}

		// Load and return the team source type
		TeamLoader loader = this.context.getTeamLoader(this);
		return loader.loadOfficeFloorTeamSourceType(this.teamName, teamSource, this.propertyList);
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

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.context.overrideProperties(this, this.teamName, this.propertyList);

		// Build the team
		String teamName = this.getQualifiedName();
		TeamBuilder<?> teamBuilder = builder.addTeam(teamName, teamSource);
		for (Property property : overriddenProperties) {
			teamBuilder.addProperty(property.getName(), property.getValue());
		}
		if (this.teamSize != null) {
			teamBuilder.setTeamSize(this.teamSize);
		}
		if (this.isRequestNoTeamOversight) {
			teamBuilder.requestNoTeamOversight();
		}
	}

	/*
	 * ============= OfficeFloorTeam ====================================
	 */

	@Override
	public String getOfficeFloorTeamName() {
		return this.getQualifiedName();
	}

	@Override
	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	@Override
	public void requestNoTeamOversight() {
		this.isRequestNoTeamOversight = true;
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
