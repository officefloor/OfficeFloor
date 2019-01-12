package net.officefloor.web.jwt;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.HttpAuthenticationImpl;
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
		AbstractHttpSecuritySource<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, JwtHttpSecuritySource.Flows> {

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
			MetaDataContext<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> context)
			throws Exception {

		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass((Class) JwtHttpAccessControl.class);

		// Provide flow to retrieve keys
		context.addFlow(Flows.RETRIEVE_KEYS, JwtDecodeCollector.class);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		return new JwtHttpSecurity();
	}

	/**
	 * JWT {@link HttpSecurity}.
	 */
	private class JwtHttpSecurity
			implements HttpSecurity<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> {

		@Override
		public HttpAuthentication<Void> createAuthentication(
				AuthenticationContext<JwtHttpAccessControl<C>, Void> context) {
			return new HttpAuthenticationImpl<>(context, null);
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