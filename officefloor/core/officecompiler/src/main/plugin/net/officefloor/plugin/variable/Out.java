package net.officefloor.plugin.variable;

/**
 * Provides output to set a variable.
 * 
 * @author Daniel Sagenschneider
 */
public interface Out<T> {

	/**
	 * Sets the value for the variable.
	 * 
	 * @param value Value for the variable.
	 */
	void set(T value);

}