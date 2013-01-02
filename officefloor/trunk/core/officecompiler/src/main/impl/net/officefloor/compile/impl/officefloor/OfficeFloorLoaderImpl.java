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
package net.officefloor.compile.impl.officefloor;

import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link OfficeFloorLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLoaderImpl implements OfficeFloorLoader {

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Mapping of {@link Profiler} by their {@link Office} name.
	 */
	private final Map<String, Profiler> profilers;

	/**
	 * Initiate.
	 * 
	 * @param nodeContext
	 *            {@link NodeContext}.
	 * @param profilers
	 *            Mapping of {@link Profiler} by their {@link Office} name.
	 */
	public OfficeFloorLoaderImpl(NodeContext nodeContext,
			Map<String, Profiler> profilers) {
		this.nodeContext = nodeContext;
		this.profilers = profilers;
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
				LocationType.OFFICE_FLOOR, null, null, null,
				this.nodeContext.getCompilerIssues());
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
			this.addIssue(
					"No "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName() + " returned from "
							+ officeFloorSourceClass.getName(), null);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeFloorSourceProperty[] officeFloorProperties;
		try {
			officeFloorProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ OfficeFloorSourceProperty.class.getSimpleName()
							+ " instances from "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName() + " for "
							+ officeFloorSourceClass.getName(), ex, null);
			return null; // failed to obtain properties
		}

		// Load the office floor properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (officeFloorProperties != null) {
			for (int i = 0; i < officeFloorProperties.length; i++) {
				OfficeFloorSourceProperty officeFloorProperty = officeFloorProperties[i];

				// Ensure have the office floor property
				if (officeFloorProperty == null) {
					this.addIssue(
							OfficeFloorSourceProperty.class.getSimpleName()
									+ " "
									+ i
									+ " is null from "
									+ OfficeFloorSourceSpecification.class
											.getSimpleName() + " for "
									+ officeFloorSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeFloorProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ OfficeFloorSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ OfficeFloorSourceSpecification.class
											.getSimpleName() + " for "
									+ officeFloorSourceClass.getName(), ex,
							null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(
							OfficeFloorSourceProperty.class.getSimpleName()
									+ " "
									+ i
									+ " provided blank name from "
									+ OfficeFloorSourceSpecification.class
											.getSimpleName() + " for "
									+ officeFloorSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeFloorProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get label for "
									+ OfficeFloorSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " ("
									+ name
									+ ") from "
									+ OfficeFloorSourceSpecification.class
											.getSimpleName() + " for "
									+ officeFloorSourceClass.getName(), ex,
							null);
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
				true, officeFloorLocation, propertyList, this.nodeContext);

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

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue(
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " "
							+ officeFloorSourceClass.getName(),
					officeFloorLocation);
			return null; // must have resource

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

		// Instantiate the OfficeFloor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(
				officeFloorSourceClass, OfficeFloorSource.class,
				LocationType.OFFICE_FLOOR, officeFloorLocation, null, null,
				this.nodeContext.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the OfficeFloor
		return this.loadOfficeFloor(officeFloorSource, officeFloorLocation,
				propertyList);
	}

	@Override
	public OfficeFloor loadOfficeFloor(OfficeFloorSource officeFloorSource,
			String officeFloorLocation, PropertyList propertyList) {

		// Create the OfficeFloor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				false, officeFloorLocation, propertyList, this.nodeContext);

		// Obtain the OfficeFloor source class for logging
		Class<?> officeFloorSourceClass = officeFloorSource.getClass();

		// Create the OfficeFloor deployer
		OfficeFloorNode deployer = new OfficeFloorNodeImpl(officeFloorLocation,
				this.nodeContext, this.profilers);

		try {
			// Source the OfficeFloor
			officeFloorSource.sourceOfficeFloor(deployer, sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSourceClass.getName(), officeFloorLocation);
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue(
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " "
							+ officeFloorSourceClass.getName(),
					officeFloorLocation);
			return null; // must have resource

		} catch (LoadTypeError ex) {
			this.addIssue("Failure loading " + ex.getType().getSimpleName()
					+ " from source " + ex.getSourceClassName(),
					officeFloorLocation);
			return null; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + OfficeFloor.class.getSimpleName()
							+ " from "
							+ OfficeFloorSource.class.getSimpleName()
							+ " (source=" + officeFloorSourceClass.getName()
							+ ", location=" + officeFloorLocation + ")", ex,
					officeFloorLocation);
			return null; // must be successful
		}

		// Deploy and return the OfficeFloor
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