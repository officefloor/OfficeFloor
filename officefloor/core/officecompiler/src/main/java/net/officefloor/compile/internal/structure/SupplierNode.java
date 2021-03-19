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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

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
	 * Loads the {@link InitialSupplierType}.
	 * 
	 * @param isLoadingType Indicates if using to load type.
	 * @return {@link InitialSupplierType}.
	 */
	InitialSupplierType loadInitialSupplierType(boolean isLoadingType);

	/**
	 * Loads the {@link SupplierType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @param isLoadingType  Indicates if using to load type.
	 * @param availableTypes {@link AvailableType} instances.
	 * @return {@link SupplierType}.
	 */
	SupplierType loadSupplierType(CompileContext compileContext, boolean isLoadingType, AvailableType[] availableTypes);

	/**
	 * Registers as a possible MBean.
	 * 
	 * @param compileContext {@link CompileContext}.
	 */
	void registerAsPossibleMBean(CompileContext compileContext);

	/**
	 * Sources the {@link SupplierThreadLocal} instances.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the {@link SupplierSource}.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceSupplier(CompileContext compileContext);

	/**
	 * Flags sourcing functionality complete.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully handle completing sourcing.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceComplete(CompileContext compileContext);

	/**
	 * <p>
	 * Ensures there are no {@link SupplierThreadLocalNode} and
	 * {@link ThreadSynchroniserFactory} instances.
	 * <p>
	 * {@link SupplierThreadLocal} and {@link ThreadSynchroniserFactory} instances
	 * are only applicable within the {@link Office} (application). If
	 * {@link SupplierSource} is used at the {@link OfficeFloor}, then it can only
	 * supply {@link ManagedObjectSource} instances and not depend on
	 * {@link SupplierThreadLocal} nor {@link ThreadSynchroniserFactory} instances.
	 * <p>
	 * If {@link SupplierThreadLocal} or {@link ThreadSynchroniserFactory} instances
	 * then they are raised via {@link CompilerIssues}.
	 *
	 * @return <code>true</code> if no {@link SupplierThreadLocal} nor
	 *         {@link ThreadSynchroniserFactory} instances.
	 * @param compileContext {@link CompileContext}.
	 */
	boolean ensureNoThreadLocals(CompileContext compileContext);

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
	 * Loads the {@link SuppliedManagedObjectSourceNode} instances as
	 * {@link ManagedObjectExtensionNode} instances to the {@link AutoWirer}.
	 * 
	 * @param autoWirer                  {@link AutoWirer}.
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 */
	void loadAutoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext);

	/**
	 * Auto-wires the {@link SupplierThreadLocalNode} instances that are unlinked.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param office         {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office, CompileContext compileContext);

	/**
	 * Builds the {@link SupplierThreadLocal} instances for the
	 * {@link SupplierSource}.
	 * 
	 * @param officeBuilder  {@link OfficeBuilder} to build the required thread
	 *                       handling.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildSupplier(OfficeBuilder officeBuilder, CompileContext compileContext);

	/**
	 * Obtains the {@link InternalSupplier} instances.
	 * 
	 * @return {@link InternalSupplier} instances.
	 */
	InternalSupplier[] getInternalSuppliers();

}
