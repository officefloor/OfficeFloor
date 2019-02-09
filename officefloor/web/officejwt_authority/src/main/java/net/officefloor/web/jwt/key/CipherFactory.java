package net.officefloor.web.jwt.key;

import javax.crypto.Cipher;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for {@link Cipher}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface CipherFactory {

	/**
	 * Allows for the {@link CipherFactory} to be configured.
	 * 
	 * @param context {@link SourceContext}.
	 */
	default void init(SourceContext context) {
		// No configuration by default
	}

	/**
	 * Creates a {@link Cipher}.
	 * 
	 * @return {@link Cipher}.
	 * @throws Exception If fails to create {@link Cipher}.
	 */
	Cipher createCipher() throws Exception;

}