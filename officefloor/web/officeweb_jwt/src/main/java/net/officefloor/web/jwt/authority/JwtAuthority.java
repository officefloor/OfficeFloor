package net.officefloor.web.jwt.authority;

/**
 * Authority for JWT.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthority<C> {

	/**
	 * Create the JWT for the claims.
	 * 
	 * @param claims Claims.
	 * @return JWT.
	 */
	String createJwt(C claims);

}