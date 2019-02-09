package net.officefloor.web.jwt.key;

import java.security.Key;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for synchronous {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface SynchronousKeyFactory {

	/**
	 * Allows for the {@link SynchronousKeyFactory} to be configured.
	 * 
	 * @param context {@link SourceContext}.
	 */
	default void init(SourceContext context) {
		// No configuration by default
	}

	/**
	 * Creates a synchronous {@link Key}.
	 * 
	 * @return Synchronous {@link Key}.
	 * @throws Exception If fails to create synchronous {@link Key}.
	 */
	Key createSynchronousKey() throws Exception;

}