/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.section;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
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
	 * {@link OfficeNode} containing the {@link OfficeSection}.
	 */
	private final OfficeNode officeNode;

	/**
	 * Parent {@link SectionNode}. May be <code>null</code> if top level
	 * {@link OfficeSection}.
	 */
	private final SectionNode parentSectionNode;

	/**
	 * {@link Node} requiring the {@link SectionLoader}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate.
	 * 
	 * @param officeNode        {@link OfficeNode} containing the
	 *                          {@link OfficeSection}.
	 * @param parentSectionNode Parent {@link SectionNode}. May be <code>null</code>
	 *                          if top level {@link OfficeSection}.
	 * @param nodeContext       {@link NodeContext}.
	 */
	public SectionLoaderImpl(OfficeNode officeNode, SectionNode parentSectionNode, NodeContext nodeContext) {
		this.officeNode = officeNode;
		this.parentSectionNode = parentSectionNode;
		this.nodeContext = nodeContext;

		// Default to appropriate node
		this.node = (this.parentSectionNode != null) ? this.parentSectionNode : this.officeNode;
	}

	/**
	 * Initiate.
	 * 
	 * @param node        {@link Node} requiring the {@link SectionLoader}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public SectionLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;

		// Provide defaults for creating the section node
		this.officeNode = new OfficeNodeImpl("<load-section-type>", null, nodeContext) {
			@Override
			public String getQualifiedName(String simpleName) {
				return simpleName; // don't qualify
			}
		};
		this.parentSectionNode = null;
	}

	/**
	 * Creates the {@link SectionNode} to load the {@link OfficeSectionType} or
	 * {@link SectionType}.
	 * 
	 * @param sectionName            Name of the {@link SectionNode}.
	 * @param sectionSourceClassName {@link SectionSource} {@link Class} name.
	 * @param sectionSource          {@link SectionSource} instance. May be
	 *                               <code>null</code>.
	 * @param sectionLocation        Location of the {@link SectionNode}.
	 * @return {@link SectionNode}.
	 */
	private SectionNode createSectionNode(String sectionName, String sectionSourceClassName,
			SectionSource sectionSource, String sectionLocation) {

		// Create the section node
		SectionNode sectionNode = (this.parentSectionNode != null)
				? this.nodeContext.createSectionNode(sectionName, this.parentSectionNode)
				: this.nodeContext.createSectionNode(sectionName, this.officeNode);
		sectionNode.initialise(sectionSourceClassName, sectionSource, sectionLocation);

		// Return the section node
		return sectionNode;
	}

	/*
	 * ====================== SectionLoader ====================================
	 */

	@Override
	public <S extends SectionSource> PropertyList loadSpecification(Class<S> sectionSourceClass) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the specification
		return this.loadSpecification(sectionSource);
	}

	@Override
	public PropertyList loadSpecification(SectionSource sectionSource) {

		// Obtain the specification
		SectionSourceSpecification specification;
		try {
			specification = sectionSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + SectionSourceSpecification.class.getSimpleName() + " from "
					+ sectionSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + SectionSourceSpecification.class.getSimpleName() + " returned from "
					+ sectionSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		SectionSourceProperty[] sectionProperties;
		try {
			sectionProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + SectionSourceProperty.class.getSimpleName() + " instances from "
					+ SectionSourceSpecification.class.getSimpleName() + " for " + sectionSource.getClass().getName(),
					ex);
			return null; // failed to obtain properties
		}

		// Load the section properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (sectionProperties != null) {
			for (int i = 0; i < sectionProperties.length; i++) {
				SectionSourceProperty sectionProperty = sectionProperties[i];

				// Ensure have the section property
				if (sectionProperty == null) {
					this.addIssue(SectionSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ SectionSourceSpecification.class.getSimpleName() + " for "
							+ sectionSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = sectionProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + SectionSourceProperty.class.getSimpleName() + " " + i
							+ " from " + SectionSourceSpecification.class.getSimpleName() + " for "
							+ sectionSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(SectionSourceProperty.class.getSimpleName() + " " + i + " provided blank name from "
							+ SectionSourceSpecification.class.getSimpleName() + " for "
							+ sectionSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = sectionProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + SectionSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + SectionSourceSpecification.class.getSimpleName() + " for "
							+ sectionSource.getClass().getName(), ex);
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
	public <S extends SectionSource> SectionType loadSectionType(String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, PropertyList propertyList) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded section type
		return this.loadSectionType(sectionName, sectionSource, sectionLocation, propertyList);
	}

	@Override
	public SectionType loadSectionType(String sectionName, SectionSource sectionSource, String sectionLocation,
			PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName(sectionName);

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the section node
		SectionNode sectionNode = this.createSectionNode(sectionName, sectionSource.getClass().getName(), sectionSource,
				sectionLocation);
		overriddenProperties.configureProperties(sectionNode);

		// Create the compile context
		CompileContext compileContext = this.nodeContext.createCompileContext();

		// Source the section
		boolean isSourced = sectionNode.sourceSection(null, null, compileContext);
		if (!isSourced) {
			return null; // must source section successfully
		}

		// Return the section type
		return sectionNode.loadSectionType(compileContext);
	}

	@Override
	public <S extends SectionSource> OfficeSectionType loadOfficeSectionType(String sectionName,
			Class<S> sectionSourceClass, String sectionLocation, PropertyList propertyList) {

		// Instantiate the section source
		SectionSource sectionSource = CompileUtil.newInstance(sectionSourceClass, SectionSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (sectionSource == null) {
			return null; // failed to instantiate
		}

		// Return loaded office section type
		return this.loadOfficeSectionType(sectionName, sectionSource, sectionLocation, propertyList);
	}

	@Override
	public OfficeSectionType loadOfficeSectionType(String sectionName, SectionSource sectionSource,
			String sectionLocation, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName(sectionName);

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the section node
		SectionNode sectionNode = this.createSectionNode(sectionName, sectionSource.getClass().getName(), sectionSource,
				sectionLocation);
		overriddenProperties.configureProperties(sectionNode);

		// Create the compile context
		CompileContext compileContext = this.nodeContext.createCompileContext();

		// Source the section
		boolean isSourced = sectionNode.sourceSectionTree(null, null, compileContext);
		if (!isSourced) {
			return null; // must source section successfully
		}

		// Return the office section type
		return sectionNode.loadOfficeSectionType(compileContext);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}