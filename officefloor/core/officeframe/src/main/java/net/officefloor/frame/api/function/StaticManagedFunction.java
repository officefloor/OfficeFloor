package net.officefloor.frame.api.function;

/**
 * Static {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class StaticManagedFunction<O extends Enum<O>, F extends Enum<F>>
		implements ManagedFunctionFactory<O, F>, ManagedFunction<O, F> {

	/*
	 * ==================== ManagedFunctionFactory =====================
	 */

	@Override
	public final ManagedFunction<O, F> createManagedFunction() throws Throwable {
		return this;
	}

}