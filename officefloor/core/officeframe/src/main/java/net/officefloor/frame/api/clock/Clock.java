package net.officefloor.frame.api.clock;

/**
 * Clock to obtain time.
 * 
 * @author Daniel Sagenschneider
 */
public interface Clock<T> {

	/**
	 * Obtains the time.
	 * 
	 * @return Time.
	 */
	T getTime();

}