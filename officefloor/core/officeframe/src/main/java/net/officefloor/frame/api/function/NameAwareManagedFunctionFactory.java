package net.officefloor.frame.api.function;

/**
 * Name aware {@link ManagedFunctionFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NameAwareManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>>
		extends ManagedFunctionFactory<O, F> {

	/**
	 * Provides the {@link ManagedFunctionFactory} the bound name for its
	 * created {@link ManagedFunction} instances.
	 * 
	 * @param boundFunctionName
	 *            Bound name for the created {@link ManagedFunction}.
	 */
	void setBoundFunctionName(String boundFunctionName);

}