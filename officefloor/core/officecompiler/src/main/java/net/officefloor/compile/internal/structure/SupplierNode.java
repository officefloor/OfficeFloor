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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Supplier {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierNode extends Node, OfficeFloorSupplier, OfficeSupplier {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Supplier";

	/**
	 * Initialises the {@link SupplierNode}.
	 * 
	 * @param supplierSourceClassName {@link Class} name of the
	 *                                {@link SupplierSource}.
	 * @param supplierSource          Optional instantiated {@link SupplierSource}.
	 *                                May be <code>null</code>.
	 */
	void initialise(String supplierSourceClassName, SupplierSource supplierSource);

	/**
	 * Obtain the qualified name.
	 * 
	 * @param simpleName Simple name to qualify with the {@link SupplierSource} name
	 *                   space.
	 * @return Qualified name.
	 */
	String getQualifiedName(String simpleName);

	/**
	 * Obtains the parent {@link OfficeNode}.
	 * 
	 * @return Parent {@link OfficeNode} or <code>null</code> if configured at the
	 *         {@link OfficeFloor} level.
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtains the parent {@link OfficeFloorNode}.
	 * 
	 * @return Parent {@link OfficeFloorNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Loads the {@link SupplierType}.
	 * 
	 * @return {@link SupplierType}.
	 */
	SupplierType loadSupplierType();

	/**
	 * Registers as a possible MBean.
	 * 
	 * @param compileContext {@link CompileContext}.
	 */
	void registerAsPossibleMBean(CompileContext compileContext);

	/**
	 * Sources the {@link SupplierThreadLocal} instances.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the {@link SupplierSource}.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceSupplier(CompileContext compileContext);

	/**
	 * Loads the {@link SuppliedManagedObjectSourceNode} instances as
	 * {@link ManagedObjectNode} instances to the {@link AutoWirer}.
	 * 
	 * @param autoWirer                  {@link AutoWirer}.
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 */
	void loadAutoWireObjects(AutoWirer<LinkObjectNode> autoWirer, ManagedObjectSourceVisitor managedObjectSourceVisitor,
			CompileContext compileContext);

	/**
	 * Builds the {@link SupplierThreadLocal} instances for the
	 * {@link SupplierSource}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 */
	void buildSupplier(CompileContext compileContext);

}