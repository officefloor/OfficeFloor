package net.officefloor.web.jwt.key;

import java.security.KeyPair;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for asynchronous {@link KeyPair}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AsynchronousKeyFactory {

	/**
	 * Allows for the {@link AsynchronousKeyFactory} to be configured.
	 * 
	 * @param context {@link SourceContext}.
	 */
	default void init(SourceContext context) {
		// No configuration by default
	}

	/**
	 * Creates an asynchronous {@link KeyPair}.
	 * 
	 * @return Asynchronous {@link KeyPair}.
	 * @throws Exception If fails to create asynchronous {@link KeyPair}.
	 */
	KeyPair createAsynchronousKeyPair() throws Exception;

}