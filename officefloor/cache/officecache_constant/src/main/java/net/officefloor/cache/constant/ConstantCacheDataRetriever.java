package net.officefloor.cache.constant;

import java.util.Map;

/**
 * Retrieves the data for the {@link ConstantCacheManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConstantCacheDataRetriever<K, V> {

	/**
	 * Obtains the data.
	 * 
	 * @return {@link Map} containing the cached key/value pairs.
	 * @throws Exception If fails to obtain data.
	 */
	Map<K, V> getData() throws Exception;

}