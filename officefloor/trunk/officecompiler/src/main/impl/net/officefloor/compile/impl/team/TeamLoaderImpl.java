/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.team;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
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
 * @author Daniel
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
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location.
	 * @param teamName
	 *            Name of the {@link Team}.
	 */
	public TeamLoaderImpl(String officeFloorLocation, String teamName) {
		this.officeFloorLocation = officeFloorLocation;
		this.teamName = teamName;
	}

	/*
	 * ====================== TeamLoader ===========================
	 */

	@Override
	public <TS extends TeamSource> PropertyList loadSpecification(
			Class<TS> teamSourceClass, CompilerIssues issues) {

		// Instantiate the team source
		TeamSource teamSource = CompileUtil
				.newInstance(teamSourceClass, TeamSource.class,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						AssetType.TEAM, this.teamName, issues);
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
					+ teamSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + TeamSourceSpecification.class.getSimpleName()
					+ " returned from " + teamSourceClass.getName(), issues);
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
					+ teamSourceClass.getName(), ex, issues);
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
							+ " for " + teamSourceClass.getName(), issues);
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
							+ " for " + teamSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(TeamSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ TeamSourceSpecification.class.getSimpleName()
							+ " for " + teamSourceClass.getName(), issues);
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
							+ " for " + teamSourceClass.getName(), ex, issues);
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
			PropertyList propertyList, ClassLoader classLoader,
			CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement TeamLoader.loadTeam");
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, CompilerIssues issues) {
		issues.addIssue(LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.TEAM, this.teamName, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			CompilerIssues issues) {
		issues.addIssue(LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.TEAM, this.teamName, issueDescription, cause);
	}

}