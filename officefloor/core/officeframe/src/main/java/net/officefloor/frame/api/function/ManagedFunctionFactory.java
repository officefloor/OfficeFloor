package net.officefloor.frame.api.function;

/**
 * Creates the {@link ManagedFunction} to be executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunction}.
	 * @throws Throwable
	 *             If fails to create the {@link ManagedFunction}.
	 */
	ManagedFunction<O, F> createManagedFunction() throws Throwable;

}