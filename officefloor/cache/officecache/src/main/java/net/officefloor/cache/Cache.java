package net.officefloor.cache;

/**
 * Common cache interface.
 * 
 * @author Daniel Sagenschneider
 */
public interface Cache<K, V> {

	/**
	 * Obtains the value for the key.
	 * 
	 * @param key Key of cached value.
	 * @return Value or <code>null</code> if no value available for key.
	 */
	V get(K key);

}