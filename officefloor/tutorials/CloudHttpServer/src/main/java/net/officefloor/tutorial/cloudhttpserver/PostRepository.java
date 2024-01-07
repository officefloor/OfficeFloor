package net.officefloor.tutorial.cloudhttpserver;

import net.officefloor.cabinet.Cabinet;
import net.officefloor.cabinet.Key;

/**
 * Repository.
 * 
 * @author Daniel Sagenschneider
 */
@Cabinet
public interface PostRepository {

	/**
	 * Stores the {@link Post}.
	 * 
	 * @param post {@link Post}.
	 */
	void store(Post post);

	/**
	 * Obtains the {@link Post} by {@link Key}.
	 * 
	 * @param key {@link Key} for {@link Post}.
	 * @return {@link Post}.
	 */
	Post getPostByKey(String key);

}
