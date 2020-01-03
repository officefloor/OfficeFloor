package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeSupplierThreadLocal;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;

/**
 * Node for a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalNode
		extends LinkObjectNode, OfficeFloorSupplierThreadLocal, OfficeSupplierThreadLocal {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplier Thread Local";

	/**
	 * Initialises the {@link SupplierThreadLocalNode}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	void initialise(OptionalThreadLocalReceiver optionalThreadLocalReceiver);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SupplierThreadLocalNode}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

	/**
	 * Builds the {@link SupplierThreadLocal}.
	 * 
	 * @param context {@link CompileContext}.
	 */
	void buildSupplierThreadLocal(CompileContext context);

}