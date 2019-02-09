package net.officefloor.web.jwt.role;

import java.util.Collection;

/**
 * Collects the roles for the JWT claims.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtRoleCollector<C> {

	/**
	 * Obtains the JWT claims.
	 * 
	 * @return JWT claims.
	 */
	C getClaims();

	/**
	 * Specifies the roles.
	 * 
	 * @param roles Roles
	 */
	void setRoles(Collection<String> roles);

	/**
	 * Indicates failure in retrieving the roles.
	 * 
	 * @param cause Cause of failure.
	 */
	void setFailure(Throwable cause);

}