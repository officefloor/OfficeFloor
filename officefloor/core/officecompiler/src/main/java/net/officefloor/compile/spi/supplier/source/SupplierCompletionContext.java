package net.officefloor.compile.spi.supplier.source;

/**
 * Completion context for the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierCompletionContext extends SupplierCompileContext {

	/**
	 * <p>
	 * Obtains the {@link AvailableType} instances.
	 * <p>
	 * These are all the {@link AvailableType} instances at the time of calling this
	 * method. {@link SupplierSource} instances may add further
	 * {@link AvailableType} instances.
	 * 
	 * @return {@link AvailableType} instances.
	 */
	AvailableType[] getAvailableTypes();
}