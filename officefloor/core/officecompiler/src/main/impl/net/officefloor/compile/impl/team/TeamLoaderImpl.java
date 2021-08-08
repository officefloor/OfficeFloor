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
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceProperty;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
import net.officefloor.frame.impl.construct.team.ExecutiveContextImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;

/**
 * {@link TeamLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamLoaderImpl implements TeamLoader, IssueTarget {

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
	 * @param node        {@link Node} requiring the {@link Team}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public TeamLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ====================== TeamLoader ===========================
	 */

	@Override
	public <TS extends TeamSource> PropertyList loadSpecification(Class<TS> teamSourceClass) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil.newInstance(teamSourceClass, TeamSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (teamSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the specification
		return this.loadSpecification(teamSource);
	}

	@Override
	public PropertyList loadSpecification(TeamSource teamSource) {

		// Obtain the specification
		TeamSourceSpecification specification;
		try {
			specification = teamSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + TeamSourceSpecification.class.getSimpleName() + " from "
					+ teamSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + TeamSourceSpecification.class.getSimpleName() + " returned from "
					+ teamSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		TeamSourceProperty[] teamSourceProperties;
		try {
			teamSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain " + TeamSourceProperty.class.getSimpleName() + " instances from "
							+ TeamSourceSpecification.class.getSimpleName() + " for " + teamSource.getClass().getName(),
					ex);
			return null; // failed to obtain properties
		}

		// Load the team source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (teamSourceProperties != null) {
			for (int i = 0; i < teamSourceProperties.length; i++) {
				TeamSourceProperty teamProperty = teamSourceProperties[i];

				// Ensure have the team source property
				if (teamProperty == null) {
					this.addIssue(TeamSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ TeamSourceSpecification.class.getSimpleName() + " for "
							+ teamSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = teamProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + TeamSourceProperty.class.getSimpleName() + " " + i
							+ " from " + TeamSourceSpecification.class.getSimpleName() + " for "
							+ teamSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(TeamSourceProperty.class.getSimpleName() + " " + i + " provided blank name from "
							+ TeamSourceSpecification.class.getSimpleName() + " for "
							+ teamSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = teamProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + TeamSourceProperty.class.getSimpleName() + " " + i + " ("
							+ name + ") from " + TeamSourceSpecification.class.getSimpleName() + " for "
							+ teamSource.getClass().getName(), ex);
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
	public <TS extends TeamSource> TeamType loadTeamType(String teamName, Class<TS> teamSourceClass,
			PropertyList propertyList) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil.newInstance(teamSourceClass, TeamSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (teamSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the team type
		return this.loadTeamType(teamName, teamSource, propertyList);
	}

	@Override
	public TeamType loadTeamType(String teamName, TeamSource teamSource, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName(teamName);

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create thread factory manufacturer
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(
				new ManagedExecutionFactoryImpl(new ThreadCompletionListener[0]), null);

		// Create the executive
		Executive executive = new DefaultExecutive(threadFactoryManufacturer);

		// Create the team (executive) context
		ExecutiveContextImpl context = new ExecutiveContextImpl(true, qualifiedName, false, 10, null, executive,
				threadFactoryManufacturer, new PropertyListSourceProperties(overriddenProperties),
				this.nodeContext.getRootSourceContext());

		// Attempt to create the team
		try {
			teamSource.createTeam(context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // can not carry on

		} catch (Throwable ex) {
			this.addIssue("Failed to initialise " + teamSource.getClass().getName(), ex);
			return null; // failed loading team
		}

		// Obtain whether required team size
		boolean isRequireTeamSize = context.isRequireTeamSize();

		// Create and return the team type
		return new TeamTypeImpl(isRequireTeamSize);
	}

	@Override
	public <TS extends TeamSource> OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(String teamName,
			Class<TS> teamSourceClass, PropertyList propertyList) {

		// Load the specification
		PropertyList properties = this.loadSpecification(teamSourceClass);

		// Load and return the type
		return this.loadOfficeFloorTeamSourceType(teamName, properties, propertyList);
	}

	@Override
	public OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(String teamName, TeamSource teamSource,
			PropertyList propertyList) {

		// Load the specification
		PropertyList properties = this.loadSpecification(teamSource);

		// Load and return the type
		return this.loadOfficeFloorTeamSourceType(teamName, properties, propertyList);
	}

	/**
	 * Loads the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @param teamName     Name of the {@link Team}.
	 * @param properties   {@link PropertyList} from specification.
	 * @param propertyList {@link PropertyList} for loading
	 *                     {@link OfficeFloorTeamSourceType}.
	 * @return {@link OfficeFloorTeamSourceType}.
	 */
	private OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(String teamName, PropertyList properties,
			PropertyList propertyList) {

		// Ensure have properties
		if (properties == null) {
			return null;
		}

		// Load the values onto the properties
		// Note: create additional optional properties as needed
		for (Property property : propertyList) {
			properties.getOrAddProperty(property.getName()).setValue(property.getValue());
		}

		// Create and return type
		return new OfficeFloorTeamSourceTypeImpl(teamName, PropertyNode.constructPropertyNodes(properties));
	}

	/*
	 * ==================== IssueTarget ==========================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
