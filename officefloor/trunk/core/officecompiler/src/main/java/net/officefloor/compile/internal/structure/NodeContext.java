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
package net.officefloor.compile.internal.structure;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Context for a node.
 * 
 * @author Daniel Sagenschneider
 */
public interface NodeContext {

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

	/**
	 * Obtains the {@link CompilerIssues}.
	 * 
	 * @return {@link CompilerIssues}.
	 */
	CompilerIssues getCompilerIssues();

	/**
	 * Obtains the {@link OfficeFrame}.
	 * 
	 * @return {@link OfficeFrame}.
	 */
	OfficeFrame getOfficeFrame();

	/**
	 * Initiates the {@link OfficeFloorBuilder} with the
	 * {@link OfficeFloorCompiler} details.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 */
	void initiateOfficeFloorBuilder(OfficeFloorBuilder builder);

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * Obtains the {@link OfficeSource} class.
	 * 
	 * @param <S>
	 *            {@link OfficeSource} type.
	 * @param officeSourceName
	 *            {@link OfficeSource} class name or an alias to an
	 *            {@link OfficeSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office} for reporting issues.
	 * @param officeName
	 *            Name of {@link Office} for reporting issues.
	 * @return {@link OfficeSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends OfficeSource> Class<S> getOfficeSourceClass(
			String officeSourceName, String officeLocation, String officeName);

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @return {@link OfficeLoader}.
	 */
	OfficeLoader getOfficeLoader();

	/**
	 * Obtains the {@link SectionSource} class.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceName
	 *            {@link SectionSource} class name or an alias to an
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} for reporting issues.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} for reporting issues.
	 * @return {@link SectionSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SectionSource> Class<S> getSectionSourceClass(
			String sectionSourceName, String sectionLocation, String sectionName);

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @return {@link SectionLoader}.
	 */
	SectionLoader getSectionLoader();

	/**
	 * Obtains the {@link WorkSource} class.
	 * 
	 * @param <S>
	 *            {@link WorkSource} type.
	 * @param workSourceName
	 *            {@link WorkSource} class name or an alias to a
	 *            {@link WorkSource} class.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} requiring the
	 *            {@link Work} for reporting issues.
	 * @param workName
	 *            Name of the {@link Work} for reporting issues.
	 * @return {@link WorkSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends WorkSource<?>> Class<S> getWorkSourceClass(
			String workSourceName, String sectionLocation, String workName);

	/**
	 * Obtains the {@link WorkLoader}.
	 * 
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} requiring the
	 *            {@link Work} for reporting issues.
	 * @param workName
	 *            Name of the {@link Work} for reporting issues.
	 * @return {@link WorkLoader}.
	 */
	WorkLoader getWorkLoader(String sectionLocation, String workName);

	/**
	 * Obtains the {@link ManagedObjectSource} class.
	 * 
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} class name or an alias to a
	 *            {@link ManagedObjectSource} class.
	 * @param locationType
	 *            {@link LocationType} for reporting issues.
	 * @param location
	 *            Location requiring the {@link ManagedObjectSource} for
	 *            reporting issues.
	 * @param managedObjectName
	 *            Name of {@link ManagedObject} for reporting issues.
	 * @return {@link ManagedObjectSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(
			String managedObjectSourceName, LocationType locationType,
			String location, String managedObjectName);

	/**
	 * Obtains the {@link ManagedObjectLoader}.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @return {@link ManagedObjectLoader}.
	 */
	ManagedObjectLoader getManagedObjectLoader(LocationType locationType,
			String location, String managedObjectName);

	/**
	 * Obtains the {@link ManagedObjectPoolLoader}.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param managedObjectPoolName
	 *            Name of the {@link ManagedObjectPool}.
	 * @return {@link ManagedObjectPoolLoader}.
	 */
	ManagedObjectPoolLoader getManagedObjectPoolLoader(
			LocationType locationType, String location,
			String managedObjectPoolName);

	/**
	 * Obtains the {@link SupplierSource} class.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} class name or an alias to a
	 *            {@link SupplierSource} class.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} requiring the
	 *            {@link OfficeFloorSupplier} for reporting issues.
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier} for reporting issues.
	 * @return {@link SupplierSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SupplierSource> Class<S> getSupplierSourceClass(
			String supplierSourceClassName, String officeFloorLocation,
			String supplierName);

	/**
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} requiring the
	 *            {@link OfficeFloorSupplier} for reporting issues.
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier} for reporting issues.
	 * @return {@link SupplierLoader}.
	 */
	SupplierLoader getSupplierLoader(String officeFloorLocation,
			String supplierName);

	/**
	 * Obtains the {@link AdministratorSource} class.
	 * 
	 * @param <S>
	 *            {@link AdministratorSource} type.
	 * @param administratorSourceName
	 *            {@link AdministratorSource} class name or an alias to an
	 *            {@link AdministratorSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office} requiring
	 *            {@link AdministratorSource} for reporting issues.
	 * @param administratorName
	 *            Name of {@link Administrator} for reporting issues.
	 * @return {@link AdministratorSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends AdministratorSource<?, ?>> Class<S> getAdministratorSourceClass(
			String administratorSourceName, String officeLocation,
			String administratorName);

	/**
	 * Obtains the {@link AdministratorLoader}.
	 * 
	 * @param officeLocation
	 *            {@link Office} location.
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @return {@link AdministratorLoader}.
	 */
	AdministratorLoader getAdministratorLoader(String officeLocation,
			String administratorName);

	/**
	 * Obtains the {@link GovernanceSource} class.
	 * 
	 * @param <S>
	 *            {@link GovernanceSource} type.
	 * @param governanceSourceName
	 *            {@link GovernanceSource} class name or an alias to an
	 *            {@link GovernanceSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office} requiring
	 *            {@link GovernanceSource} for reporting issues.
	 * @param governanceName
	 *            Name of {@link Governance} for reporting issues.
	 * @return {@link GovernanceSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(
			String governanceSourceName, String officeLocation,
			String governanceName);

	/**
	 * Obtains the {@link GovernanceLoader}.
	 * 
	 * @param officeLocation
	 *            {@link Office} location.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @return {@link GovernanceLoader}.
	 */
	GovernanceLoader getGovernanceLoader(String officeLocation,
			String governanceName);

	/**
	 * Obtains the {@link TeamSource} class.
	 * 
	 * @param <S>
	 *            {@link TeamSource} type.
	 * @param teamSourceName
	 *            {@link TeamSource} class name or an alias to a
	 *            {@link TeamSource} class.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} requiring the
	 *            {@link TeamSource} for reporting issues.
	 * @param teamName
	 *            Name of {@link Team} for reporting issues.
	 * @return {@link TeamSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends TeamSource> Class<S> getTeamSourceClass(String teamSourceName,
			String officeFloorLocation, String teamName);

}