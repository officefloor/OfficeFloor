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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.NodeContextImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link OfficeLoader} implementation.
 * 
 * @author Daniel
 */
public class OfficeLoaderImpl implements OfficeLoader {

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	public OfficeLoaderImpl(String officeLocation) {
		this.officeLocation = officeLocation;
	}

	/*
	 * ========================= OfficeLoader ==================================
	 */

	@Override
	public <O extends OfficeSource> PropertyList loadSpecification(
			Class<O> officeSourceClass, CompilerIssues issues) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass,
				OfficeSource.class, LocationType.OFFICE, this.officeLocation,
				null, null, issues);
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		OfficeSourceSpecification specification;
		try {
			specification = officeSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ OfficeSourceSpecification.class.getSimpleName()
					+ " from " + officeSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ OfficeSourceSpecification.class.getSimpleName()
					+ " returned from " + officeSourceClass.getName(), issues);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeSourceProperty[] officeProperties;
		try {
			officeProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ OfficeSourceProperty.class.getSimpleName()
					+ " instances from "
					+ OfficeSourceSpecification.class.getSimpleName() + " for "
					+ officeSourceClass.getName(), ex, issues);
			return null; // failed to obtain properties
		}

		// Load the office properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (officeProperties != null) {
			for (int i = 0; i < officeProperties.length; i++) {
				OfficeSourceProperty officeProperty = officeProperties[i];

				// Ensure have the office property
				if (officeProperty == null) {
					this.addIssue(OfficeSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " for " + officeSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeProperty.getName();
				} catch (Throwable ex) {
					this
							.addIssue("Failed to get name for "
									+ OfficeSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ OfficeSourceSpecification.class
											.getSimpleName() + " for "
									+ officeSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(OfficeSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " for " + officeSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeProperty.getLabel();
				} catch (Throwable ex) {
					this
							.addIssue("Failed to get label for "
									+ OfficeSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " ("
									+ name
									+ ") from "
									+ OfficeSourceSpecification.class
											.getSimpleName() + " for "
									+ officeSourceClass.getName(), ex, issues);
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
	public <O extends OfficeSource> OfficeType loadOfficeType(
			Class<O> officeSourceClass,
			ConfigurationContext configurationContext,
			PropertyList propertyList, ClassLoader classLoader,
			CompilerIssues issues) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass,
				OfficeSource.class, LocationType.OFFICE, this.officeLocation,
				null, null, issues);
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Create the office type
		NodeContext nodeContext = new NodeContextImpl(configurationContext,
				classLoader, issues);
		OfficeNode officeType = new OfficeNodeImpl(this.officeLocation,
				nodeContext);

		// Load the office which in turn provides the detail for the office type
		boolean isLoaded = officeType.loadOffice(officeSource, propertyList);
		if (!isLoaded) {
			return null; // must load office
		}

		// Return the office type
		return officeType;
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
		issues.addIssue(LocationType.OFFICE, this.officeLocation, null, null,
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
		issues.addIssue(LocationType.OFFICE, this.officeLocation, null, null,
				issueDescription, cause);
	}

}