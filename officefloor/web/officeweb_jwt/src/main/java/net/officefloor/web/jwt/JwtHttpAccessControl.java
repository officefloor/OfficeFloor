package net.officefloor.web.jwt;

import net.officefloor.web.security.HttpAccessControl;

/**
 * Extra JWT functions to {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtHttpAccessControl<C> extends HttpAccessControl {

	/**
	 * Obtains the JWT claims.
	 * 
	 * @return JWT claims.
	 */
	C getClaims();

}