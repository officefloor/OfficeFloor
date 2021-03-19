/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.office;

import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.configuration.impl.ConfigurationSourceContextImpl;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceContextImpl extends ConfigurationSourceContextImpl
		implements OfficeSourceContext, OfficeExtensionContext {

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
	 * @param isLoadingType      Indicates if loading type.
	 * @param officeLocation     Location of the {@link Office}.
	 * @param additionalProfiles Additional profiles.
	 * @param propertyList       {@link PropertyList}.
	 * @param officeNode         {@link OfficeNode}.
	 * @param nodeContext        {@link NodeContext}.
	 */
	public OfficeSourceContextImpl(boolean isLoadingType, String officeLocation, String[] additionalProfiles,
			PropertyList propertyList, OfficeNode officeNode, NodeContext nodeContext) {
		super(officeNode.getQualifiedName(), isLoadingType, nodeContext.getRootSourceContext(), additionalProfiles,
				new PropertyListSourceProperties(propertyList));
		this.officeLocation = officeLocation;
		this.officeNode = officeNode;
		this.context = nodeContext;
	}

	/*
	 * =============== OfficeExtensionContext ===============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeNode.getDeployedOfficeName();
	}

	/*
	 * ================= OfficeSourceContext ================================
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
					SectionNode sectionNode = this.context.createSectionNode(sectionName, this.officeNode);
					Class sectionSourceClass = this.context.getSectionSourceClass(sectionSourceClassName, sectionNode);
					if (sectionSourceClass == null) {
						return null;
					}

					// Load and return the section type
					SectionLoader sectionLoader = this.context.getSectionLoader(this.officeNode);
					return sectionLoader.loadOfficeSectionType(sectionName, sectionSourceClass, sectionLocation,
							properties);
				});
	}

	@Override
	public OfficeSectionType loadOfficeSectionType(String sectionName, SectionSource sectionSource,
			String sectionLocation, PropertyList properties) {
		return CompileUtil.loadType(OfficeSectionType.class, sectionSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the section type
					SectionLoader sectionLoader = this.context.getSectionLoader(this.officeNode);
					return sectionLoader.loadOfficeSectionType(sectionName, sectionSource, sectionLocation, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.officeNode);
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, mosNode);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, properties);
				});
	}

	@Override
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the managed object type
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.officeNode);
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSource, properties);
				});
	}

	@Override
	public InitialSupplierType loadSupplierType(String supplierName, SupplierSource supplierSource,
			PropertyList properties) {
		return CompileUtil.loadType(InitialSupplierType.class, supplierSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the supplier type
					SupplierNode supplierNode = this.context.createSupplierNode(supplierName, this.officeNode);
					SupplierLoader supplierLoader = this.context.getSupplierLoader(supplierNode, true);
					return supplierLoader.loadInitialSupplierType(supplierSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InitialSupplierType loadSupplierType(String supplierName, String supplierSourceClassName,
			PropertyList properties) {
		return CompileUtil.loadType(InitialSupplierType.class, supplierSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the supplier source class
					SupplierNode supplierNode = this.context.createSupplierNode(supplierName, this.officeNode);
					Class supplierSourceClass = this.context.getSupplierSourceClass(supplierSourceClassName,
							supplierNode);
					if (supplierSourceClass == null) {
						return null;
					}

					// Load and return the supplier type
					SupplierLoader supplierLoader = this.context.getSupplierLoader(supplierNode, true);
					return supplierLoader.loadInitialSupplierType(supplierSourceClass, properties);
				});
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GovernanceType<?, ?> loadGovernanceType(String governanceName, String governanceSourceClassName,
			PropertyList properties) {
		return CompileUtil.loadType(GovernanceType.class, governanceSourceClassName, this.context.getCompilerIssues(),
				() -> {

					// Obtain the governance source class
					GovernanceNode governanceNode = this.context.createGovernanceNode(governanceName, this.officeNode);
					Class governanceSourceClass = this.context.getGovernanceSourceClass(governanceSourceClassName,
							governanceNode);
					if (governanceSourceClass == null) {
						return null;
					}

					// Load and return the governance type
					GovernanceLoader governanceLoader = this.context.getGovernanceLoader(governanceNode, true);
					return governanceLoader.loadGovernanceType(governanceSourceClass, properties);
				});
	}

	@Override
	public GovernanceType<?, ?> loadGovernanceType(String governanceName, GovernanceSource<?, ?> governanceSource,
			PropertyList properties) {
		return CompileUtil.loadType(GovernanceType.class, governanceSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the governance type
					GovernanceNode governanceNode = this.context.createGovernanceNode(governanceName, this.officeNode);
					GovernanceLoader governanceLoader = this.context.getGovernanceLoader(governanceNode, true);
					return governanceLoader.loadGovernanceType(governanceSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministrationType<?, ?, ?> loadAdministrationType(String administrationName,
			String administrationSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(AdministrationType.class, administrationSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the administrator source class
					AdministrationNode adminNode = this.context.createAdministrationNode(administrationName,
							this.officeNode);
					Class administratorSourceClass = this.context
							.getAdministrationSourceClass(administrationSourceClassName, adminNode);
					if (administratorSourceClass == null) {
						return null;
					}

					// Load and return the administrator type
					AdministrationLoader administratorLoader = this.context.getAdministrationLoader(adminNode, true);
					return administratorLoader.loadAdministrationType(administratorSourceClass, properties);
				});
	}

	@Override
	public AdministrationType<?, ?, ?> loadAdministrationType(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource, PropertyList properties) {
		return CompileUtil.loadType(AdministrationType.class, administrationSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the administration type
					AdministrationNode adminNode = this.context.createAdministrationNode(administrationName,
							this.officeNode);
					AdministrationLoader administrationLoader = this.context.getAdministrationLoader(adminNode, true);
					return administrationLoader.loadAdministrationType(administrationSource, properties);
				});
	}

}
