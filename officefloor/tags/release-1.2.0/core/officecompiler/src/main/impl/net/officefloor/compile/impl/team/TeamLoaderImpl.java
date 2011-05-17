/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceProperty;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;

/**
 * {@link TeamLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamLoaderImpl implements TeamLoader {

	/**
	 * Location.
	 */
	private final String officeFloorLocation;

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate for building.
	 * 
	 * @param officeFloorLocation
	 *            Location.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public TeamLoaderImpl(String officeFloorLocation, String teamName,
			NodeContext nodeContext) {
		this.officeFloorLocation = officeFloorLocation;
		this.teamName = teamName;
		this.nodeContext = nodeContext;
	}

	/**
	 * Initiate from {@link OfficeFloorCompiler}.
	 * 
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public TeamLoaderImpl(NodeContext nodeContext) {
		this(null, null, nodeContext);
	}

	/*
	 * ====================== TeamLoader ===========================
	 */

	@Override
	public <TS extends TeamSource> PropertyList loadSpecification(
			Class<TS> teamSourceClass) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil.newInstance(teamSourceClass,
				TeamSource.class, LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, AssetType.TEAM, this.teamName,
				this.nodeContext.getCompilerIssues());
		if (teamSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		TeamSourceSpecification specification;
		try {
			specification = teamSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ TeamSourceSpecification.class.getSimpleName() + " from "
					+ teamSourceClass.getName(), ex);
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
			this.addIssue("Failed to obtain "
					+ TeamSourceProperty.class.getSimpleName()
					+ " instances from "
					+ TeamSourceSpecification.class.getSimpleName() + " for "
					+ teamSourceClass.getName(), ex);
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
					this.addIssue("Failed to get name for "
							+ TeamSourceProperty.class.getSimpleName() + " "
							+ i + " from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName(), ex);
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
	public <TS extends TeamSource> TeamType loadTeam(Class<TS> teamSourceClass,
			PropertyList propertyList) {

		// TODO test this method (loadTeam)

		return new TeamTypeImpl();
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.TEAM, this.teamName, issueDescription);
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
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.TEAM, this.teamName, issueDescription, cause);
	}

}