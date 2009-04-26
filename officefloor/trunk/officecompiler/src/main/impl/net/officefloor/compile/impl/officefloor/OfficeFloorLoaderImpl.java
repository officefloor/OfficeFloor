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
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.ConfigurationContextPropagateError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorUnknownPropertyError;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorLoader} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorLoaderImpl implements OfficeFloorLoader {

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
	public OfficeFloorLoaderImpl(NodeContext nodeContext) {
		this.nodeContext = nodeContext;
	}

	/*
	 * ======================= OfficeFloorLoader ===========================
	 */

	@Override
	public <OF extends OfficeFloorSource> PropertyList loadSpecification(
			Class<OF> officeFloorSourceClass) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(
				officeFloorSourceClass, OfficeFloorSource.class,
				LocationType.OFFICE_FLOOR, null, null, null, this.nodeContext
						.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		OfficeFloorSourceSpecification specification;
		try {
			specification = officeFloorSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ OfficeFloorSourceSpecification.class.getSimpleName()
					+ " from " + officeFloorSourceClass.getName(), ex, null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ OfficeFloorSourceSpecification.class.getSimpleName()
					+ " returned from " + officeFloorSourceClass.getName(),
					null);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeFloorSourceProperty[] officeFloorProperties;
		try {
			officeFloorProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ OfficeFloorSourceProperty.class.getSimpleName()
					+ " instances from "
					+ OfficeFloorSourceSpecification.class.getSimpleName()
					+ " for " + officeFloorSourceClass.getName(), ex, null);
			return null; // failed to obtain properties
		}

		// Load the office floor properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (officeFloorProperties != null) {
			for (int i = 0; i < officeFloorProperties.length; i++) {
				OfficeFloorSourceProperty officeFloorProperty = officeFloorProperties[i];

				// Ensure have the office floor property
				if (officeFloorProperty == null) {
					this.addIssue(OfficeFloorSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ officeFloorSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeFloorProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ OfficeFloorSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " from "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName() + " for "
							+ officeFloorSourceClass.getName(), ex, null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(OfficeFloorSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ officeFloorSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeFloorProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ OfficeFloorSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " ("
							+ name
							+ ") from "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName() + " for "
							+ officeFloorSourceClass.getName(), ex, null);
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
	public <OF extends OfficeFloorSource> PropertyList loadRequiredProperties(
			Class<OF> officeFloorSourceClass, final String officeFloorLocation,
			PropertyList propertyList) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(
				officeFloorSourceClass, OfficeFloorSource.class,
				LocationType.OFFICE_FLOOR, officeFloorLocation, null, null,
				this.nodeContext.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Create the office floor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				officeFloorLocation,
				this.nodeContext.getConfigurationContext(), propertyList,
				this.nodeContext.getClassLoader());

		// Create the required properties
		final PropertyList requiredPropertyList = new PropertyListImpl();
		final boolean[] isRequiredPropertyIssue = new boolean[1];
		RequiredProperties requiredProperties = new RequiredProperties() {
			@Override
			public void addRequiredProperty(String name) {
				this.addRequiredProperty(name, null);
			}

			@Override
			public void addRequiredProperty(String name, String label) {

				// Ensure have name
				if (CompileUtil.isBlank(name)) {
					OfficeFloorLoaderImpl.this.addIssue(
							"Required property specified with null name (label="
									+ label + ")", officeFloorLocation);
					isRequiredPropertyIssue[0] = true;
					return; // must have name
				}

				// Determine if already added the property
				if (requiredPropertyList.getProperty(name) != null) {
					OfficeFloorLoaderImpl.this.addIssue("Required property "
							+ name + " already added", officeFloorLocation);
					isRequiredPropertyIssue[0] = true;
					return; // must only register required property once
				}

				// Add the required property
				requiredPropertyList.addProperty(name, label);
			}
		};

		try {
			// Specify the configuration properties
			officeFloorSource.specifyConfigurationProperties(
					requiredProperties, sourceContext);

		} catch (OfficeFloorUnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknonwnPropertyName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have property

		} catch (ConfigurationContextPropagateError ex) {
			this.addIssue("Failure obtaining configuration '"
					+ ex.getLocation() + "'", ex.getCause(),
					officeFloorLocation);
			return null; // must not fail in getting configurations

		} catch (Throwable ex) {
			this.addIssue("Failed to source required properties from "
					+ OfficeFloorSource.class.getSimpleName() + " (source="
					+ officeFloorSourceClass.getName() + ", location="
					+ officeFloorLocation + ")", ex, officeFloorLocation);
			return null; // must be successful
		}

		// Do not return if issue with required properties
		if (isRequiredPropertyIssue[0]) {
			return null; // issue with required properties
		}

		// Return the listing of required properties
		return requiredPropertyList;
	}

	@Override
	public <OF extends OfficeFloorSource> OfficeFloor loadOfficeFloor(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(
				officeFloorSourceClass, OfficeFloorSource.class,
				LocationType.OFFICE_FLOOR, officeFloorLocation, null, null,
				this.nodeContext.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Create the office floor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				officeFloorLocation,
				this.nodeContext.getConfigurationContext(), propertyList,
				this.nodeContext.getClassLoader());

		// Create the office floor deployer
		OfficeFloorNode deployer = new OfficeFloorNodeImpl(officeFloorLocation,
				this.nodeContext);

		try {
			// Source the office floor
			officeFloorSource.sourceOfficeFloor(deployer, sourceContext);

		} catch (OfficeFloorUnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknonwnPropertyName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have property

		} catch (ConfigurationContextPropagateError ex) {
			this.addIssue("Failure obtaining configuration '"
					+ ex.getLocation() + "'", ex.getCause(),
					officeFloorLocation);
			return null; // must not fail in getting configurations

		} catch (Throwable ex) {
			this.addIssue("Failed to source "
					+ OfficeFloor.class.getSimpleName() + " from "
					+ OfficeFloorSource.class.getSimpleName() + " (source="
					+ officeFloorSourceClass.getName() + ", location="
					+ officeFloorLocation + ")", ex, officeFloorLocation);
			return null; // must be successful
		}

		// Deploy and return the office floor
		return deployer.deployOfficeFloor(this.nodeContext.getOfficeFrame());
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	private void addIssue(String issueDescription, String officeFloorLocation) {
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, officeFloorLocation, null, null,
				issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			String officeFloorLocation) {
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, officeFloorLocation, null, null,
				issueDescription, cause);
	}

}