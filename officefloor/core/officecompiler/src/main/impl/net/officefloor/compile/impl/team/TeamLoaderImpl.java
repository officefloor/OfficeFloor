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
package net.officefloor.compile.impl.team;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.structure.PropertyNode;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamIdentifier;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceProperty;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
import net.officefloor.frame.impl.construct.team.TeamSourceContextImpl;

/**
 * {@link TeamLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamLoaderImpl implements TeamLoader {

	/**
	 * {@link TeamIdentifier} for the {@link TeamType}.
	 */
	private static final TeamIdentifier TYPE_TEAM = new TeamIdentifier() {
	};

	/**
	 * {@link Node} requiring the {@link Team}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate for building.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link Team}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public TeamLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ====================== TeamLoader ===========================
	 */

	@Override
	public <TS extends TeamSource> PropertyList loadSpecification(
			Class<TS> teamSourceClass) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil.newInstance(teamSourceClass,
				TeamSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (teamSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		TeamSourceSpecification specification;
		try {
			specification = teamSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " from " + teamSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + TeamSourceSpecification.class.getSimpleName()
					+ " returned from " + teamSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		TeamSourceProperty[] teamSourceProperties;
		try {
			teamSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ TeamSourceProperty.class.getSimpleName()
							+ " instances from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the team source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (teamSourceProperties != null) {
			for (int i = 0; i < teamSourceProperties.length; i++) {
				TeamSourceProperty teamProperty = teamSourceProperties[i];

				// Ensure have the team source property
				if (teamProperty == null) {
					this.addIssue(TeamSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = teamProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ TeamSourceProperty.class.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ TeamSourceSpecification.class
											.getSimpleName() + " for "
									+ teamSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(TeamSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = teamProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ TeamSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName(), ex);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <TS extends TeamSource> TeamType loadTeamType(String teamName,
			Class<TS> teamSourceClass, PropertyList propertyList) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil.newInstance(teamSourceClass,
				TeamSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (teamSource == null) {
			return null; // failed to instantiate
		}

		// Attempt to create the team
		try {
			teamSource.createTeam(new TeamSourceContextImpl(true, teamName,
					TYPE_TEAM, new PropertyListSourceProperties(propertyList),
					this.nodeContext.getRootSourceContext()));

		} catch (UnknownPropertyError ex) {
			this.nodeContext.getCompilerIssues().addIssue(this.node,
					"Missing property '" + ex.getUnknownPropertyName() + "'");
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.nodeContext.getCompilerIssues().addIssue(this.node,
					"Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.nodeContext.getCompilerIssues().addIssue(
					this.node,
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "'");
			return null; // must have resource

		} catch (Throwable ex) {
			this.nodeContext.getCompilerIssues().addIssue(this.node,
					"Failed to initialise " + teamSourceClass.getName(), ex);
			return null; // failed loading team
		}

		// Create and return the team type
		return new TeamTypeImpl();
	}

	@Override
	public <TS extends TeamSource> OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(
			String teamName, Class<TS> teamSourceClass,
			PropertyList propertyList) {

		// Load the specification
		PropertyList properties = this.loadSpecification(teamSourceClass);
		if (properties == null) {
			return null;
		}

		// Load the values onto the properties
		// Note: create additional optional properties as needed
		for (Property property : propertyList) {
			properties.getOrAddProperty(property.getName()).setValue(
					property.getValue());
		}

		// Create and return type
		return new OfficeFloorTeamSourceTypeImpl(teamName,
				PropertyNode.constructPropertyNodes(properties));
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription, cause);
	}

}