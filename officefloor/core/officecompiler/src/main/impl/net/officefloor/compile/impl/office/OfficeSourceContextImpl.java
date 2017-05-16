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

import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link OfficeSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceContextImpl extends SourceContextImpl implements OfficeSourceContext, OfficeExtensionContext {

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Instantiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param officeNode
	 *            {@link OfficeNode}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeSourceContextImpl(boolean isLoadingType, String officeLocation, PropertyList propertyList,
			OfficeNode officeNode, NodeContext nodeContext) {
		super(isLoadingType, nodeContext.getRootSourceContext(), new PropertyListSourceProperties(propertyList));
		this.officeLocation = officeLocation;
		this.officeNode = officeNode;
		this.context = nodeContext;
	}

	/*
	 * ================= OfficeLoaderContext ================================
	 */

	@Override
	public String getOfficeLocation() {
		return this.officeLocation;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeSectionType loadOfficeSectionType(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties) {
		return CompileUtil.loadType(OfficeSectionType.class, sectionSourceClassName, this.context.getCompilerIssues(),
				() -> {

					// Obtain the section source class
					Class sectionSourceClass = this.context.getSectionSourceClass(sectionSourceClassName,
							this.officeNode);
					if (sectionSourceClass == null) {
						return null;
					}

					// Obtain the overridden properties
					String qualifiedSectionName = this.officeNode.getQualifiedName(sectionName);
					PropertyList overriddenProperties = this.context.overrideProperties(this.officeNode,
							qualifiedSectionName, properties);

					// Load and return the section type
					SectionLoader sectionLoader = this.context.getSectionLoader(this.officeNode);
					return sectionLoader.loadOfficeSectionType(sectionName, sectionSourceClass, sectionLocation,
							overriddenProperties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, this.officeNode);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this.officeNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, properties);
				});
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GovernanceType<?, ?> loadGovernanceType(String governanceSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(GovernanceType.class, governanceSourceClassName, this.context.getCompilerIssues(),
				() -> {

					// Obtain the governance source class
					Class governanceSourceClass = this.context.getGovernanceSourceClass(governanceSourceClassName,
							this.officeNode);
					if (governanceSourceClass == null) {
						return null;
					}

					// Load and return the governance type
					GovernanceLoader governanceLoader = this.context.getGovernanceLoader(this.officeNode);
					return governanceLoader.loadGovernanceType(governanceSourceClass, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministrationType<?, ?, ?> loadAdministrationType(String administratorSourceClassName,
			PropertyList properties) {
		return CompileUtil.loadType(AdministrationType.class, administratorSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the administrator source class
					Class administratorSourceClass = this.context
							.getAdministrationSourceClass(administratorSourceClassName, this.officeNode);
					if (administratorSourceClass == null) {
						return null;
					}

					// Load and return the administrator type
					AdministrationLoader administratorLoader = this.context.getAdministrationLoader(this.officeNode);
					return administratorLoader.loadAdministrationType(administratorSourceClass, properties);
				});
	}

}