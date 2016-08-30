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
import net.officefloor.compile.impl.structure.PropertyNode;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.Property;
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
	 * {@link Node} requiring the {@link OfficeFloor}.
	 */
	private final Node node;

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
	 * @param node
	 *            {@link Node} requiring the {@link OfficeFloor}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 * @param profilers
	 *            Mapping of {@link Profiler} by their {@link Office} name.
	 */
	public OfficeFloorLoaderImpl(Node node, NodeContext nodeContext,
			Map<String, Profiler> profilers) {
		this.node = node;
		this.nodeContext = nodeContext;
		this.profilers = profilers;
	}

	/*
	 * ======================= OfficeFloorLoader ===========================
	 */

	/**
	 * Instantiates the {@link OfficeFloorSource}.
	 * 
	 * @param officeFloorSourceClass
	 *            {@link OfficeFloorSource} {@link Class}.
	 * @param officeFloorLocation
	 *            Optional location of the {@link OfficeFloor}. May be
	 *            <code>null</code>.
	 * @return {@link OfficeFloorSource} or <code>null</code> if issue.
	 */
	private <OF extends OfficeFloorSource> OfficeFloorSource newOfficeFloorSource(
			Class<OF> officeFloorSourceClass, String officeFloorLocation) {
		return CompileUtil.newInstance(officeFloorSourceClass,
				OfficeFloorSource.class, this.node,
				this.nodeContext.getCompilerIssues());
	}

	@Override
	public <OF extends OfficeFloorSource> PropertyList loadSpecification(
			Class<OF> officeFloorSourceClass) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = this.newOfficeFloorSource(
				officeFloorSourceClass, null);
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the specification
		return this.loadSpecification(officeFloorSource);
	}

	/**
	 * Loads the {@link PropertyList} specification from the
	 * {@link OfficeFloorSource} instance.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource}.
	 * @return {@link PropertyList} specification or <code>null</code> if issue.
	 */
	private PropertyList loadSpecification(OfficeFloorSource officeFloorSource) {

		// Obtain the specification
		OfficeFloorSourceSpecification specification;
		try {
			specification = officeFloorSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ OfficeFloorSourceSpecification.class.getSimpleName()
					+ " from " + officeFloorSource.getClass().getName(), ex,
					null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue(
					"No "
							+ OfficeFloorSourceSpecification.class
									.getSimpleName() + " returned from "
							+ officeFloorSource.getClass().getName(), null);
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
							+ officeFloorSource.getClass().getName(), ex, null);
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
									+ officeFloorSource.getClass().getName(),
							null);
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
									+ officeFloorSource.getClass().getName(),
							ex, null);
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
									+ officeFloorSource.getClass().getName(),
							null);
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
									+ officeFloorSource.getClass().getName(),
							ex, null);
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
		OfficeFloorSource officeFloorSource = this.newOfficeFloorSource(
				officeFloorSourceClass, officeFloorLocation);
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the required properties
		return this.loadRequiredProperties(officeFloorSource,
				officeFloorLocation, propertyList);
	}

	/**
	 * Loads the required {@link Property} instances.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} to configure the {@link OfficeFloor}.
	 * @return Required {@link Property} instances or <code>null</code> if issue
	 *         loading.
	 */
	private PropertyList loadRequiredProperties(
			OfficeFloorSource officeFloorSource,
			final String officeFloorLocation, PropertyList propertyList) {

		// Create the office floor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				true, officeFloorLocation, propertyList, this.node,
				this.nodeContext);

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
					+ officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue(
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " "
							+ officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to source required properties from "
					+ OfficeFloorSource.class.getSimpleName() + " (source="
					+ officeFloorSource.getClass().getName() + ", location="
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
	public <OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = this.newOfficeFloorSource(
				officeFloorSourceClass, officeFloorLocation);
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Load the specification properties
		PropertyList properties = this.loadSpecification(officeFloorSource);

		// Load the required properties
		PropertyList requiredProperties = this.loadRequiredProperties(
				officeFloorSource, officeFloorLocation, propertyList);
		for (Property property : requiredProperties) {
			String propertyName = property.getName();
			if (properties.getProperty(propertyName) == null) {
				properties.addProperty(propertyName, property.getLabel());
			}
		}

		// Load optional properties
		for (Property property : propertyList) {
			properties.getOrAddProperty(property.getName()).setValue(
					property.getValue());
		}

		// Load the OfficeFloor type
		OfficeFloorNode node = this.loadOfficeFloorNode(officeFloorSource,
				officeFloorLocation, propertyList, true);
		if (node == null) {
			return null; // must be loaded
		}
		boolean isLoaded = node.loadOfficeFloorType(PropertyNode
				.constructPropertyNodes(properties));
		if (!isLoaded) {
			return null; // must be loaded
		}

		// Return loaded office floor type
		return node.getOfficeFloorType();
	}

	@Override
	public <OF extends OfficeFloorSource> OfficeFloor loadOfficeFloor(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList) {

		// Instantiate the OfficeFloor source
		OfficeFloorSource officeFloorSource = this.newOfficeFloorSource(
				officeFloorSourceClass, officeFloorLocation);
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

		// Load the deployer (office floor node)
		OfficeFloorNode deployer = this.loadOfficeFloorNode(officeFloorSource,
				officeFloorLocation, propertyList, false);
		if (deployer == null) {
			return null;
		}

		// Deploy and return the OfficeFloor
		return deployer.deployOfficeFloor(this.nodeContext.getOfficeFrame());
	}

	/**
	 * Loads the {@link OfficeFloorNode}.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 * @param propertyList
	 *            {@link PropertyList} to configure the
	 *            {@link OfficeFloorSource}.
	 * @return Loaded {@link OfficeFloorNode}.
	 */
	private OfficeFloorNode loadOfficeFloorNode(
			OfficeFloorSource officeFloorSource, String officeFloorLocation,
			PropertyList propertyList, boolean isLoadingType) {

		// Create the OfficeFloor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				false, officeFloorLocation, propertyList, this.node,
				this.nodeContext);

		// Obtain the OfficeFloor source class for logging
		Class<?> officeFloorSourceClass = officeFloorSource.getClass();

		// Create the OfficeFloor node
		OfficeFloorNode node = new OfficeFloorNodeImpl(officeFloorLocation,
				this.nodeContext, this.profilers);

		try {
			// Source the OfficeFloor
			officeFloorSource.sourceOfficeFloor(node, sourceContext);

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
					isLoadingType ? "Failed to source OfficeFloorType definition from OfficeFloorSource "
							+ officeFloorSourceClass.getName()
							: "Failed to source "
									+ OfficeFloor.class.getSimpleName()
									+ " from "
									+ OfficeFloorSource.class.getSimpleName()
									+ " (source="
									+ officeFloorSourceClass.getName()
									+ ", location=" + officeFloorLocation + ")",
					ex, officeFloorLocation);
			return null; // must be successful
		}

		// Return the office floor node
		return node;
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
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			String officeFloorLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription, cause);
	}

}