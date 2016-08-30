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
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceProperty;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;

/**
 * {@link SectionLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionLoaderImpl implements SectionLoader {

	/**
	 * {@link Node} requiring the {@link OfficeSection}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link OfficeSection}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public SectionLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
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
				sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
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
				sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded section type
		return this.loadSectionType(sectionSource, sectionLocation,
				propertyList);
	}

	@Override
	public SectionType loadSectionType(SectionSource sectionSource,
			String sectionLocation, PropertyList propertyList) {

		// Create the section node
		SectionNode sectionNode = this.nodeContext.createSectionNode(
				Node.TYPE_NAME, (OfficeNode) null).initialise(
				sectionSource.getClass().getName(), sectionSource,
				sectionLocation);
		propertyList.configureProperties(sectionNode);

		// Source the section
		boolean isSourced = sectionNode.sourceSection();
		if (!isSourced) {
			return null; // must source section successfully
		}

		// Return the section type
		return sectionNode.loadSectionType();
	}

	@Override
	public <S extends SectionSource> OfficeSectionType loadOfficeSectionType(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, PropertyList propertyList) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(
				sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded office section type
		return this.loadOfficeSectionType(sectionName, sectionSource,
				sectionLocation, propertyList);
	}

	@Override
	public OfficeSectionType loadOfficeSectionType(String sectionName,
			SectionSource sectionSource, String sectionLocation,
			PropertyList propertyList) {

		// Create the section node
		SectionNode sectionNode = this.nodeContext.createSectionNode(
				sectionName, (OfficeNode) null).initialise(
				sectionSource.getClass().getName(), sectionSource,
				sectionLocation);
		propertyList.configureProperties(sectionNode);

		// Source the section
		boolean isSourced = sectionNode.sourceSectionTree();
		if (!isSourced) {
			return null; // must source section successfully
		}

		// Return the office section type
		return sectionNode.loadOfficeSectionType();
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
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			String sectionLocation) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription, cause);
	}

}