package net.officefloor.cabinet;

import java.util.Optional;

/**
 * Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinet<D> {

	/**
	 * Retrieves the document by key.
	 * 
	 * @param key Key.
	 * @return {@link Optional} to possibly retrieved entity.
	 */
	Optional<D> retrieveByKey(String key);

	/**
	 * Stores the document.
	 * 
	 * @param document Document.
	 */
	void store(D document);

}