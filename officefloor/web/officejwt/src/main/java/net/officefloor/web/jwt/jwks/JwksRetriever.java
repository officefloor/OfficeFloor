package net.officefloor.web.jwt.jwks;

import java.io.InputStream;

/**
 * Retrieves the JWKS content.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksRetriever {

	/**
	 * Retrieves the JWKS content.
	 * 
	 * @return {@link InputStream} to the JWKS content.
	 * @throws Exception If fails to retrieve the JWKS content.
	 */
	InputStream retrieveJwks() throws Exception;

}