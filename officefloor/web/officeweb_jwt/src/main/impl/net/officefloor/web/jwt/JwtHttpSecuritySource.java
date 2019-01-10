package net.officefloor.web.jwt;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.jwt.spi.JwtKeyCollector;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpSecuritySource} for JWT.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtHttpSecuritySource<C> extends
		AbstractHttpSecuritySource<JwtHttpAuthentication<C>, JwtHttpAccessControl<C>, Void, None, JwtHttpSecuritySource.Flows> {

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		RETRIEVE_KEYS
	}

	/*
	 * ==================== HttpSecuritySource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(
			MetaDataContext<JwtHttpAuthentication<C>, JwtHttpAccessControl<C>, Void, None, Flows> context)
			throws Exception {

		context.setAuthenticationClass((Class) JwtHttpAuthentication.class);
		context.setAccessControlClass((Class) JwtHttpAccessControl.class);

		// Provide flow to retrieve keys
		context.addFlow(Flows.RETRIEVE_KEYS, JwtKeyCollector.class);
	}

	@Override
	public HttpSecurity<JwtHttpAuthentication<C>, JwtHttpAccessControl<C>, Void, None, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		return new JwtHttpSecurity();
	}

	/**
	 * JWT {@link HttpSecurity}.
	 */
	private class JwtHttpSecurity
			implements HttpSecurity<JwtHttpAuthentication<C>, JwtHttpAccessControl<C>, Void, None, Flows> {

		@Override
		public JwtHttpAuthentication<C> createAuthentication(
				AuthenticationContext<JwtHttpAccessControl<C>, Void> context) {
			return new JwtHttpAuthenticationImpl<C>(context);
		}

		@Override
		public boolean ratify(Void credentials, RatifyContext<JwtHttpAccessControl<C>> context) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void authenticate(Void credentials, AuthenticateContext<JwtHttpAccessControl<C>, None> context)
				throws HttpException {
			// TODO Auto-generated method stub

		}

		@Override
		public void challenge(ChallengeContext<None, Flows> context) throws HttpException {
			// TODO Auto-generated method stub
		}

		@Override
		public void logout(LogoutContext<None> context) throws HttpException {
			// TODO Auto-generated method stub

		}
	}

}