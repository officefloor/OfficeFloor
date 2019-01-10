package net.officefloor.web.jwt;

import net.officefloor.web.security.scheme.HttpAuthenticationImpl;
import net.officefloor.web.spi.security.AuthenticationContext;

/**
 * {@link JwtHttpAuthentication} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtHttpAuthenticationImpl<C> extends HttpAuthenticationImpl<Void> implements JwtHttpAuthentication<C> {

	/**
	 * Instantiate.
	 * 
	 * @param authenticationContext {@link AuthenticationContext}.
	 */
	public JwtHttpAuthenticationImpl(AuthenticationContext<JwtHttpAccessControl<C>, Void> authenticationContext) {
		super(authenticationContext, Void.class);
	}

	/*
	 * ============== JwtHttpAuthentication ===================
	 */

	@Override
	public String createJwt(C claims) {
		// TODO Auto-generated method stub
		return null;
	}

}