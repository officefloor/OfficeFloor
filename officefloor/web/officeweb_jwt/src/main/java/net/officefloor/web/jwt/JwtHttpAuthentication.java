package net.officefloor.web.jwt;

import net.officefloor.web.security.HttpAuthentication;

/**
 * Extra JWT functions to {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtHttpAuthentication<C> extends HttpAuthentication<Void> {

	/**
	 * Creates the JWS (signed JWT).
	 * 
	 * @param claims Claims.
	 * @return JWS (signed JWT).
	 */
	String createJwt(C claims);

}