package net.officefloor.frame.api.function;

/**
 * Managed function.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunction<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Executes the function.
	 * 
	 * @param context {@link ManagedFunctionContext} for the
	 *                {@link ManagedFunction}.
	 * @throws Throwable Indicating failure of the {@link ManagedFunction}.
	 */
	void execute(ManagedFunctionContext<O, F> context) throws Throwable;

}