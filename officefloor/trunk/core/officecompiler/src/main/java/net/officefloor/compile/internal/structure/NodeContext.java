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
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Context for a {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NodeContext {

	/**
	 * Obtains the root {@link SourceContext}.
	 * 
	 * @return Root {@link SourceContext}.
	 */
	SourceContext getRootSourceContext();

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
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name or an alias to an
	 *            {@link OfficeSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link OfficeSource} class.
	 * @return {@link OfficeSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends OfficeSource> Class<S> getOfficeSourceClass(
			String officeSourceClassName, Node node);

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link OfficeLoader}.
	 * @return {@link OfficeLoader}.
	 */
	OfficeLoader getOfficeLoader(Node node);

	/**
	 * Obtains the {@link SectionSource} class.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name or an alias to an
	 *            {@link SectionSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link SectionSource} class.
	 * @return {@link SectionSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SectionSource> Class<S> getSectionSourceClass(
			String sectionSourceClassName, Node node);

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link SectionLoader}.
	 * @return {@link SectionLoader}.
	 */
	SectionLoader getSectionLoader(Node node);

	/**
	 * Creates a {@link SectionNode}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param office
	 *            {@link OfficeNode} containing this {@link OfficeSection}.
	 * @return {@link SectionNode}.
	 */
	SectionNode createSectionNode(String sectionName, OfficeNode office);

	/**
	 * Obtains the {@link WorkSource} class.
	 * 
	 * @param <S>
	 *            {@link WorkSource} type.
	 * @param workSourceName
	 *            {@link WorkSource} class name or an alias to a
	 *            {@link WorkSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link WorkSource} class.
	 * @return {@link WorkSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends WorkSource<?>> Class<S> getWorkSourceClass(
			String workSourceName, Node node);

	/**
	 * Obtains the {@link WorkLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link WorkLoader}.
	 * @return {@link WorkLoader}.
	 */
	WorkLoader getWorkLoader(Node node);

	/**
	 * Obtains the {@link ManagedObjectSource} class.
	 * 
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} class name or an alias to a
	 *            {@link ManagedObjectSource} class.
	 * @param managedObjectName
	 *            Name of {@link ManagedObject} for reporting issues.
	 * @return {@link ManagedObjectSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(
			String managedObjectSourceName, Node node);

	/**
	 * Obtains the {@link ManagedObjectLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link ManagedObjectLoader}.
	 * @return {@link ManagedObjectLoader}.
	 */
	ManagedObjectLoader getManagedObjectLoader(Node node);

	/**
	 * Obtains the {@link ManagedObjectPoolLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link ManagedObjectPoolLoader}.
	 * @return {@link ManagedObjectPoolLoader}.
	 */
	ManagedObjectPoolLoader getManagedObjectPoolLoader(Node node);

	/**
	 * Obtains the {@link SupplierSource} class.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} class name or an alias to a
	 *            {@link SupplierSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link SupplierSource} class.
	 * @return {@link SupplierSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SupplierSource> Class<S> getSupplierSourceClass(
			String supplierSourceClassName, Node node);

	/**
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link SupplierLoader}.
	 * @return {@link SupplierLoader}.
	 */
	SupplierLoader getSupplierLoader(Node node);

	/**
	 * Obtains the {@link AdministratorSource} class.
	 * 
	 * @param <S>
	 *            {@link AdministratorSource} type.
	 * @param administratorSourceClassName
	 *            {@link AdministratorSource} class name or an alias to an
	 *            {@link AdministratorSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link AdministratorSource} class.
	 * @return {@link AdministratorSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends AdministratorSource<?, ?>> Class<S> getAdministratorSourceClass(
			String administratorSourceClassName, Node node);

	/**
	 * Obtains the {@link AdministratorLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link AdministratorLoader}.
	 * @return {@link AdministratorLoader}.
	 */
	AdministratorLoader getAdministratorLoader(Node node);

	/**
	 * Obtains the {@link GovernanceSource} class.
	 * 
	 * @param <S>
	 *            {@link GovernanceSource} type.
	 * @param governanceSourceName
	 *            {@link GovernanceSource} class name or an alias to an
	 *            {@link GovernanceSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link GovernanceSource} class.
	 * @return {@link GovernanceSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(
			String governanceSourceName, Node node);

	/**
	 * Obtains the {@link GovernanceLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link GovernanceLoader}.
	 * @return {@link GovernanceLoader}.
	 */
	GovernanceLoader getGovernanceLoader(Node node);

	/**
	 * Obtains the {@link TeamSource} class.
	 * 
	 * @param <S>
	 *            {@link TeamSource} type.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name or an alias to a
	 *            {@link TeamSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link TeamSource} class.
	 * @return {@link TeamSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends TeamSource> Class<S> getTeamSourceClass(
			String teamSourceClassName, Node node);

	/**
	 * Obtains the {@link TeamLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link TeamLoader}.
	 * @return {@link TeamLoader}.
	 */
	TeamLoader getTeamLoader(Node node);

}