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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * {@link AdministratorLoader} implementation.
 * 
 * @author Daniel
 */
public class AdministratorLoaderImpl implements AdministratorLoader {

	/**
	 * {@link LocationType}.
	 */
	private LocationType locationType;

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * Name of the {@link Administrator}.
	 */
	private final String administratorName;

	/**
	 * Initiate.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 */
	public AdministratorLoaderImpl(LocationType locationType, String location,
			String administratorName) {
		this.locationType = locationType;
		this.location = location;
		this.administratorName = administratorName;
	}

	/*
	 * ===================== AdministratorLoader =============================
	 */

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> PropertyList loadSpecification(
			Class<AS> administratorSourceClass, CompilerIssues issues) {

		// Instantiate the administrator source
		AdministratorSource<I, A> administratorSource = CompileUtil
				.newInstance(administratorSourceClass,
						AdministratorSource.class, this.locationType,
						this.location, AssetType.ADMINISTRATOR,
						this.administratorName, issues);
		if (administratorSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		AdministratorSourceSpecification specification;
		try {
			specification = administratorSource.getSpecification();
		} catch (Throwable ex) {
			this
					.addIssue("Failed to obtain "
							+ AdministratorSourceSpecification.class
									.getSimpleName() + " from "
							+ administratorSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ AdministratorSourceSpecification.class.getSimpleName()
					+ " returned from " + administratorSourceClass.getName(),
					issues);
			return null; // no specification obtained
		}

		// Obtain the properties
		AdministratorSourceProperty[] administratorSourceProperties;
		try {
			administratorSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ AdministratorSourceProperty.class.getSimpleName()
					+ " instances from "
					+ AdministratorSourceSpecification.class.getSimpleName()
					+ " for " + administratorSourceClass.getName(), ex, issues);
			return null; // failed to obtain properties
		}

		// Load the administrator source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (administratorSourceProperties != null) {
			for (int i = 0; i < administratorSourceProperties.length; i++) {
				AdministratorSourceProperty adminProperty = administratorSourceProperties[i];

				// Ensure have the administrator source property
				if (adminProperty == null) {
					this.addIssue(AdministratorSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ AdministratorSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ administratorSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = adminProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ AdministratorSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " from "
							+ AdministratorSourceSpecification.class
									.getSimpleName() + " for "
							+ administratorSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(AdministratorSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ AdministratorSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ administratorSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = adminProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ AdministratorSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " ("
							+ name
							+ ") from "
							+ AdministratorSourceSpecification.class
									.getSimpleName() + " for "
							+ administratorSourceClass.getName(), ex, issues);
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
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorType<I, A> loadAdministrator(
			Class<AS> administratorSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement AdministratorLoader.loadAdministrator");
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
		issues.addIssue(this.locationType, this.location,
				AssetType.ADMINISTRATOR, this.administratorName,
				issueDescription);
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
		issues.addIssue(this.locationType, this.location,
				AssetType.ADMINISTRATOR, this.administratorName,
				issueDescription, cause);
	}

}