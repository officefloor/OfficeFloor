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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeLoaderImpl implements OfficeLoader {

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate.
	 * 
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeLoaderImpl(NodeContext nodeContext) {
		this.nodeContext = nodeContext;
	}

	/*
	 * ========================= OfficeLoader ==================================
	 */

	@Override
	public <O extends OfficeSource> PropertyList loadSpecification(
			Class<O> officeSourceClass) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass,
				OfficeSource.class, LocationType.OFFICE, null, null, null,
				this.nodeContext.getCompilerIssues());
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		OfficeSourceSpecification specification;
		try {
			specification = officeSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " from " + officeSourceClass.getName(), ex, null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue(
					"No " + OfficeSourceSpecification.class.getSimpleName()
							+ " returned from " + officeSourceClass.getName(),
					null);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeSourceProperty[] officeProperties;
		try {
			officeProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ OfficeSourceProperty.class.getSimpleName()
							+ " instances from "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " for " + officeSourceClass.getName(), ex, null);
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
							+ " for " + officeSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ OfficeSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ OfficeSourceSpecification.class
											.getSimpleName() + " for "
									+ officeSourceClass.getName(), ex, null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(OfficeSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " for " + officeSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ OfficeSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from "
							+ OfficeSourceSpecification.class.getSimpleName()
							+ " for " + officeSourceClass.getName(), ex, null);
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
			Class<O> officeSourceClass, String officeLocation,
			PropertyList propertyList) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass,
				OfficeSource.class, LocationType.OFFICE, officeLocation, null,
				null, this.nodeContext.getCompilerIssues());
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded office type
		return this.loadOfficeType(officeSource, officeLocation, propertyList);
	}

	@Override
	public OfficeType loadOfficeType(OfficeSource officeSource,
			String officeLocation, PropertyList propertyList) {

		// Create the office type
		OfficeNode officeType = new OfficeNodeImpl(officeLocation,
				this.nodeContext);

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
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	private void addIssue(String issueDescription, String officeLocation) {
		this.nodeContext.getCompilerIssues().addIssue(LocationType.OFFICE,
				officeLocation, null, null, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			String officeLocation) {
		this.nodeContext.getCompilerIssues().addIssue(LocationType.OFFICE,
				officeLocation, null, null, issueDescription, cause);
	}

}