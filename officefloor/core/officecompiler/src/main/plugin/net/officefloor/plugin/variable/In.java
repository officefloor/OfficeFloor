package net.officefloor.plugin.variable;

/**
 * Provides input to retrieve variable value.
 * 
 * @author Daniel Sagenschneider
 */
public interface In<T> {

	/**
	 * Retrieves the value for the variable.
	 * 
	 * @return Value for the variable.
	 */
	T get();

}