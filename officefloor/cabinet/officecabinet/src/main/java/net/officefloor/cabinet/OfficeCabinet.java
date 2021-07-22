package net.officefloor.cabinet;

import java.util.Optional;

/**
 * Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinet<F> {

	/**
	 * Retrieves the entity by identifier.
	 * 
	 * @param id Identifier.
	 * @return {@link Optional} to possibly retrieved entity.
	 */
	Optional<F> retrieveById(String id);

	/**
	 * Stores the entity.
	 * 
	 * @param entity Entity.
	 */
	void store(F entity);

}