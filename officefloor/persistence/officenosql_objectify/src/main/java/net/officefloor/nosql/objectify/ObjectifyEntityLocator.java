package net.officefloor.nosql.objectify;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;

/**
 * Locates {@link Objectify} {@link Entity} types for registering.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectifyEntityLocator {

	/**
	 * Locates the {@link Objectify} {@link Entity} types.
	 * 
	 * @return {@link Objectify} {@link Entity} types.
	 * @throws Exception If fails to locate the {@link Objectify} {@link Entity}
	 *                   types.
	 */
	Class<?>[] locateEntities() throws Exception;

}