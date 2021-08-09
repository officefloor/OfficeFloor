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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
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
	 * {@link Node} requiring the {@link Office}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate.
	 * 
	 * @param node        {@link Node} requiring the {@link Office}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public OfficeLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ========================= OfficeLoader ==================================
	 */

	@Override
	public <O extends OfficeSource> PropertyList loadSpecification(Class<O> officeSourceClass) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass, OfficeSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		OfficeSourceSpecification specification;
		try {
			specification = officeSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + OfficeSourceSpecification.class.getSimpleName() + " from "
					+ officeSourceClass.getName(), ex, null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + OfficeSourceSpecification.class.getSimpleName() + " returned from "
					+ officeSourceClass.getName(), null);
			return null; // no specification obtained
		}

		// Obtain the properties
		OfficeSourceProperty[] officeProperties;
		try {
			officeProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain " + OfficeSourceProperty.class.getSimpleName() + " instances from "
							+ OfficeSourceSpecification.class.getSimpleName() + " for " + officeSourceClass.getName(),
					ex, null);
			return null; // failed to obtain properties
		}

		// Load the office properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (officeProperties != null) {
			for (int i = 0; i < officeProperties.length; i++) {
				OfficeSourceProperty officeProperty = officeProperties[i];

				// Ensure have the office property
				if (officeProperty == null) {
					this.addIssue(OfficeSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ OfficeSourceSpecification.class.getSimpleName() + " for " + officeSourceClass.getName(),
							null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = officeProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + OfficeSourceProperty.class.getSimpleName() + " " + i
							+ " from " + OfficeSourceSpecification.class.getSimpleName() + " for "
							+ officeSourceClass.getName(), ex, null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(OfficeSourceProperty.class.getSimpleName() + " " + i + " provided blank name from "
							+ OfficeSourceSpecification.class.getSimpleName() + " for " + officeSourceClass.getName(),
							null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = officeProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + OfficeSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + OfficeSourceSpecification.class.getSimpleName() + " for "
							+ officeSourceClass.getName(), ex, null);
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
	public <O extends OfficeSource> OfficeType loadOfficeType(Class<O> officeSourceClass, String officeLocation,
			PropertyList propertyList) {

		// Instantiate the office source
		OfficeSource officeSource = CompileUtil.newInstance(officeSourceClass, OfficeSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (officeSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded office type
		return this.loadOfficeType(officeSource, officeLocation, propertyList);
	}

	@Override
	public OfficeType loadOfficeType(OfficeSource officeSource, String officeLocation, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the office node
		OfficeNode officeNode = this.nodeContext.createOfficeNode(this.node.getNodeName(), null);
		officeNode.initialise(officeSource.getClass().getName(), officeSource, officeLocation);

		// Configure the office node
		overriddenProperties.configureProperties(officeNode);

		// Create the compile context
		CompileContext compileContext = this.nodeContext.createCompileContext();

		// Source the office tree
		boolean isSourced = officeNode.sourceOfficeWithTopLevelSections(null, compileContext);
		if (!isSourced) {
			return null; // must source office successfully
		}

		// Return the office type
		return officeNode.loadOfficeType(compileContext);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param officeLocation   Location of the {@link Office}.
	 */
	private void addIssue(String issueDescription, String officeLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of the issue.
	 * @param officeLocation   Location of the {@link Office}.
	 */
	private void addIssue(String issueDescription, Throwable cause, String officeLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
