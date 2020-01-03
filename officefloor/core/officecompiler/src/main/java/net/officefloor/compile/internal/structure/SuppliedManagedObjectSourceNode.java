package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Node for the supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectSourceNode extends Node {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplied Managed Object Source";

	/**
	 * Initialises the {@link SuppliedManagedObjectSourceNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link SuppliedManagedObjectSourceType}. May be <code>null</code>
	 *         if issue in loading the {@link SuppliedManagedObjectSourceType}.
	 */
	SuppliedManagedObjectSourceType loadSuppliedManagedObjectSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SuppliedManagedObjectSource}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

}