package net.officefloor.compile.spi.supplier.source;

/**
 * Invoke on completion of compiling.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface SupplierCompileCompletion {

	/**
	 * Invoked on completion of compiling.
	 * 
	 * @param context {@link SupplierCompileContext}.
	 * @throws Exception Failure in handling completion.
	 */
	void complete(SupplierCompileContext context) throws Exception;

}