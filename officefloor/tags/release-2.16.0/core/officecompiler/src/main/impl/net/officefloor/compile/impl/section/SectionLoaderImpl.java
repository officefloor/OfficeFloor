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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceProperty;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link SectionLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionLoaderImpl implements SectionLoader {

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
	public SectionLoaderImpl(NodeContext nodeContext) {
		this.nodeContext = nodeContext;
	}

	/*
	 * ====================== SectionLoader ====================================
	 */

	@Override
	public <S extends SectionSource> PropertyList loadSpecification(
			Class<S> sectionSourceClass) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(
				sectionSourceClass, SectionSource.class, LocationType.SECTION,
				null, null, null, this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		SectionSourceSpecification specification;
		try {
			specification = sectionSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ SectionSourceSpecification.class.getSimpleName()
					+ " from " + sectionSourceClass.getName(), ex, null);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue(
					"No " + SectionSourceSpecification.class.getSimpleName()
							+ " returned from " + sectionSourceClass.getName(),
					null);
			return null; // no specification obtained
		}

		// Obtain the properties
		SectionSourceProperty[] sectionProperties;
		try {
			sectionProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ SectionSourceProperty.class.getSimpleName()
							+ " instances from "
							+ SectionSourceSpecification.class.getSimpleName()
							+ " for " + sectionSourceClass.getName(), ex, null);
			return null; // failed to obtain properties
		}

		// Load the section properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (sectionProperties != null) {
			for (int i = 0; i < sectionProperties.length; i++) {
				SectionSourceProperty sectionProperty = sectionProperties[i];

				// Ensure have the section property
				if (sectionProperty == null) {
					this.addIssue(SectionSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ SectionSourceSpecification.class.getSimpleName()
							+ " for " + sectionSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = sectionProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ SectionSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ SectionSourceSpecification.class
											.getSimpleName() + " for "
									+ sectionSourceClass.getName(), ex, null);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(SectionSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ SectionSourceSpecification.class.getSimpleName()
							+ " for " + sectionSourceClass.getName(), null);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = sectionProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ SectionSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from "
							+ SectionSourceSpecification.class.getSimpleName()
							+ " for " + sectionSourceClass.getName(), ex, null);
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
	public <S extends SectionSource> SectionType loadSectionType(
			Class<S> sectionSourceClass, String sectionLocation,
			PropertyList propertyList) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(
				sectionSourceClass, SectionSource.class, LocationType.SECTION,
				sectionLocation, null, null,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Create the section source context
		SectionSourceContext context = new SectionSourceContextImpl(true,
				sectionLocation, propertyList, this.nodeContext);

		// Create the section designer
		SectionNode sectionType = new SectionNodeImpl(
				SectionType.class.getSimpleName(), sectionLocation,
				this.nodeContext);

		try {
			// Source the section type
			sectionSource.sourceSection(sectionType, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + SectionSource.class.getSimpleName() + " "
					+ sectionSourceClass.getName(), sectionLocation);
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + SectionSource.class.getSimpleName() + " "
					+ sectionSourceClass.getName(), sectionLocation);
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue(
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "' for "
							+ SectionSource.class.getSimpleName() + " "
							+ sectionSourceClass.getName(), sectionLocation);
			return null; // must have resource

		} catch (LoadTypeError ex) {
			this.addIssue("Failure loading " + ex.getType().getSimpleName()
					+ " from source " + ex.getSourceClassName(),
					sectionLocation);
			return null; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + SectionType.class.getSimpleName()
							+ " definition from "
							+ SectionSource.class.getSimpleName() + " "
							+ sectionSourceClass.getName(), ex, sectionLocation);
			return null; // must be successful
		}

		// Ensure all inputs have names
		SectionInputType[] inputs = sectionType.getSectionInputTypes();
		for (int i = 0; i < inputs.length; i++) {
			if (CompileUtil.isBlank(inputs[i].getSectionInputName())) {
				this.addIssue("Null name for input " + i, sectionLocation);
				return null; // must have names for inputs
			}
		}

		// Ensure all outputs have names
		SectionOutputType[] outputs = sectionType.getSectionOutputTypes();
		for (int i = 0; i < outputs.length; i++) {
			if (CompileUtil.isBlank(outputs[i].getSectionOutputName())) {
				this.addIssue("Null name for output " + i, sectionLocation);
				return null; // must have names for outputs
			}
		}

		// Ensure all objects have names and types
		SectionObjectType[] objects = sectionType.getSectionObjectTypes();
		for (int i = 0; i < objects.length; i++) {
			SectionObjectType object = objects[i];
			if (CompileUtil.isBlank(object.getSectionObjectName())) {
				this.addIssue("Null name for object " + i, sectionLocation);
				return null; // must have names for objects
			}
			if (CompileUtil.isBlank(object.getObjectType())) {
				this.addIssue(
						"Null type for object " + i + " (name="
								+ object.getSectionObjectName() + ")",
						sectionLocation);
				return null; // must have types for objects
			}
		}

		// Return the section type
		return sectionType;
	}

	@Override
	public <S extends SectionSource> OfficeSection loadOfficeSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, PropertyList propertyList) {

		// Instantiate an instance of the section source
		S sectionSource = CompileUtil.newInstance(sectionSourceClass,
				SectionSource.class, LocationType.SECTION, sectionLocation,
				null, null, this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // must instantiate section source
		}

		// Create the section node (loading in its properties)
		SectionNode sectionNode = new SectionNodeImpl(sectionName,
				sectionSource, sectionLocation, propertyList, null,
				this.nodeContext);

		// Recursive load all the section nodes
		sectionNode.loadOfficeSection(sectionLocation);

		// Return the section node as the office section
		return sectionNode;
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	private void addIssue(String issueDescription, String sectionLocation) {
		this.nodeContext.getCompilerIssues().addIssue(LocationType.SECTION,
				sectionLocation, null, null, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			String sectionLocation) {
		this.nodeContext.getCompilerIssues().addIssue(LocationType.SECTION,
				sectionLocation, null, null, issueDescription, cause);
	}

}