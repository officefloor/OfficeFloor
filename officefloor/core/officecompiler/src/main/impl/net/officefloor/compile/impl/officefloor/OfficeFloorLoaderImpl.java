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

package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
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
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;

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
	 * Initiate.
	 * 
	 * @param node        {@link Node} requiring the {@link OfficeFloor}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public OfficeFloorLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ======================= OfficeFloorLoader ===========================
	 */

	@Override
	public <OF extends OfficeFloorSource> PropertyList loadSpecification(Class<OF> officeFloorSourceClass) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(officeFloorSourceClass, OfficeFloorSource.class,
				this.node, this.nodeContext.getCompilerIssues());
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
	 * @param officeFloorSource {@link OfficeFloorSource}.
	 * @return {@link PropertyList} specification or <code>null</code> if issue.
	 */
	private PropertyList loadSpecification(OfficeFloorSource officeFloorSource) {

		// Obtain the specification
		OfficeFloorSourceSpecification specification;
		try {
			specification = officeFloorSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + OfficeFloorSourceSpecification.class.getSimpleName() + " from "
					+ officeFloorSource.getClass().getName(), ex, null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + OfficeFloorSourceSpecification.class.getSimpleName() + " returned from "
					+ officeFloorSource.getClass().getName(), null);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeFloorSourceProperty[] officeFloorProperties;
		try {
			officeFloorProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + OfficeFloorSourceProperty.class.getSimpleName() + " instances from "
					+ OfficeFloorSourceSpecification.class.getSimpleName() + " for "
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
					this.addIssue(OfficeFloorSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ OfficeFloorSourceSpecification.class.getSimpleName() + " for "
							+ officeFloorSource.getClass().getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeFloorProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + OfficeFloorSourceProperty.class.getSimpleName() + " " + i
							+ " from " + OfficeFloorSourceSpecification.class.getSimpleName() + " for "
							+ officeFloorSource.getClass().getName(), ex, null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(OfficeFloorSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + OfficeFloorSourceSpecification.class.getSimpleName()
							+ " for " + officeFloorSource.getClass().getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeFloorProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + OfficeFloorSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + OfficeFloorSourceSpecification.class.getSimpleName() + " for "
							+ officeFloorSource.getClass().getName(), ex, null);
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
	public <OF extends OfficeFloorSource> PropertyList loadRequiredProperties(Class<OF> officeFloorSourceClass,
			final String officeFloorLocation, PropertyList propertyList) {

		// Instantiate the office floor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(officeFloorSourceClass, OfficeFloorSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the required properties
		return this.loadRequiredProperties(officeFloorSource, officeFloorLocation, propertyList);
	}

	/**
	 * Loads the required {@link Property} instances.
	 * 
	 * @param officeFloorSource   {@link OfficeFloorSource}.
	 * @param officeFloorLocation Location of the {@link OfficeFloor}.
	 * @param propertyList        {@link PropertyList} to configure the
	 *                            {@link OfficeFloor}.
	 * @return Required {@link Property} instances or <code>null</code> if issue
	 *         loading.
	 */
	private PropertyList loadRequiredProperties(OfficeFloorSource officeFloorSource, final String officeFloorLocation,
			PropertyList propertyList) {

		// Create the OfficeFloor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(true, officeFloorLocation, null,
				propertyList,
				this.nodeContext.createOfficeFloorNode("<officefloor>", officeFloorSource, officeFloorLocation),
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
							"Required property specified with null name (label=" + label + ")", officeFloorLocation);
					isRequiredPropertyIssue[0] = true;
					return; // must have name
				}

				// Determine if already added the property
				if (requiredPropertyList.getProperty(name) != null) {
					OfficeFloorLoaderImpl.this.addIssue("Required property " + name + " already added",
							officeFloorLocation);
					isRequiredPropertyIssue[0] = true;
					return; // must only register required property once
				}

				// Add the required property
				requiredPropertyList.addProperty(name, label);
			}
		};

		try {
			// Specify the configuration properties
			officeFloorSource.specifyConfigurationProperties(requiredProperties, sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue(
					"Missing property '" + ex.getUnknownPropertyName() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " " + officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue(
					"Can not load class '" + ex.getUnknownClassName() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " " + officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue(
					"Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "' for "
							+ OfficeFloorSource.class.getSimpleName() + " " + officeFloorSource.getClass().getName(),
					officeFloorLocation);
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source required properties from " + OfficeFloorSource.class.getSimpleName() + " (source="
							+ officeFloorSource.getClass().getName() + ", location=" + officeFloorLocation + ")",
					ex, officeFloorLocation);
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
	public <OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(Class<OF> officeFloorSourceClass,
			String officeFloorLocation, PropertyList propertyList) {

		// Instantiate the OfficeFloor source
		OfficeFloorSource officeFloorSource = CompileUtil.newInstance(officeFloorSourceClass, OfficeFloorSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (officeFloorSource == null) {
			return null; // failed to instantiate
		}

		// Return the OfficeFloor type
		return this.loadOfficeFloorType(officeFloorSource, officeFloorLocation, propertyList);
	}

	@Override
	public <OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(OF officeFloorSource,
			String officeFloorLocation, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName(OfficeFloorNode.OFFICE_FLOOR_NAME);

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the compile context
		CompileContext compileContext = this.nodeContext.createCompileContext();

		// Source the OfficeFloor
		OfficeFloorNode node = this.nodeContext.createOfficeFloorNode(officeFloorSource.getClass().getName(),
				officeFloorSource, officeFloorLocation);
		overriddenProperties.configureProperties(node);
		boolean isSourced = node.sourceOfficeFloor(compileContext);
		if (!isSourced) {
			return null; // must be sourced
		}

		// Load and return the OfficeFloor type
		return node.loadOfficeFloorType(compileContext);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription    Description of the issue.
	 * @param officeFloorLocation Location of the {@link OfficeFloor}.
	 */
	private void addIssue(String issueDescription, String officeFloorLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription    Description of the issue.
	 * @param cause               Cause of the issue.
	 * @param officeFloorLocation Location of the {@link OfficeFloor}.
	 */
	private void addIssue(String issueDescription, Throwable cause, String officeFloorLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
