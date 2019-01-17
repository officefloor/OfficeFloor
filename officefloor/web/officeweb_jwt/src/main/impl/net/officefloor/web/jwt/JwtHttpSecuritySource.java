package net.officefloor.web.jwt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.managedobject.poll.StatePoller;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.HttpAuthenticationImpl;
import net.officefloor.web.security.scheme.HttpAuthenticationScheme;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link HttpSecuritySource} for JWT.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtHttpSecuritySource<C> extends
		AbstractHttpSecuritySource<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, JwtHttpSecuritySource.Flows>
		implements
		HttpSecurity<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, JwtHttpSecuritySource.Flows> {

	/**
	 * Authentication scheme Bearer.
	 */
	public static final String AUTHENTICATION_SCHEME_BASIC = "Bearer";

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		RETRIEVE_KEYS, NO_JWT
	}

	/**
	 * {@link HttpRequestState} attribute name for the {@link ChallengeReason}.
	 */
	private static final String CHALLENGE_ATTRIBUTE_NAME = "challenge.reason";

	/**
	 * {@link StatePoller} to keep the {@link JwtDecoder} up to date with
	 * appropriate keys.
	 */
	private StatePoller<JwtDecoder, None> jwtDecoder;

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

		// Provide challenge flows
		context.addFlow(Flows.NO_JWT, null);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		return this;
	}

	@Override
	public void start(HttpSecurityExecuteContext<Flows> context) throws Exception {

		// Create poller for JWT decoder
		this.jwtDecoder = StatePoller.builder(JwtDecoder.class, (delay, pollContext, callback) -> {
			if (delay == 0) {
				context.registerStartupProcess(Flows.RETRIEVE_KEYS, new JwtDecodeCollectorImpl(), callback);
			} else {
				context.invokeProcess(Flows.RETRIEVE_KEYS, new JwtDecodeCollectorImpl(), delay, callback);
			}
		}).identifier("JWT decode keys").build();
	}

	/*
	 * ====================== HttpSecurity ==========================
	 */

	@Override
	public HttpAuthentication<Void> createAuthentication(AuthenticationContext<JwtHttpAccessControl<C>, Void> context) {
		return new HttpAuthenticationImpl<>(context, null);
	}

	@Override
	public boolean ratify(Void credentials, RatifyContext<JwtHttpAccessControl<C>> context) {

		// Determine if bearer credentials on request
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getRequest());
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME_BASIC.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {
			context.getRequestState().setAttribute(context.getQualifiedAttributeName(CHALLENGE_ATTRIBUTE_NAME),
					ChallengeReason.NO_JWT);
			return false; // no JWT
		}

		// Has JWT so enough information to authenticate
		return true;
	}

	@Override
	public void authenticate(Void credentials, AuthenticateContext<JwtHttpAccessControl<C>, None> context)
			throws HttpException {

		// Obtain the JWT decoder (allow time to initialise keys)
		JwtDecoder decoder;
		try {
			decoder = this.jwtDecoder.getState(1, TimeUnit.SECONDS);
		} catch (TimeoutException ex) {
			context.accessControlChange(null, new HttpException(HttpStatus.SERVICE_UNAVAILABLE,
					new TimeoutException("Server timed out loading JWT keys")));
			return; // must obtain decoder
		}

	}

	@Override
	public void challenge(ChallengeContext<None, Flows> context) throws HttpException {

		HttpResponse response = context.getConnection().getResponse();
		response.setStatus(HttpStatus.UNAUTHORIZED);

		// Determine cause of challenge
		ChallengeReason reason = (ChallengeReason) context.getRequestState()
				.getAttribute(context.getQualifiedAttributeName(CHALLENGE_ATTRIBUTE_NAME));
		switch (reason) {
		case NO_JWT:
			context.doFlow(Flows.NO_JWT);
			break;
		}
	}

	@Override
	public void logout(LogoutContext<None> context) throws HttpException {
		// TODO Auto-generated method stub

	}

	private static class KeyHolder {

	}

	/**
	 * Challenge reason.
	 */
	private static enum ChallengeReason {
		NO_JWT
	}

	/**
	 * JWT decoder.
	 */
	private static class JwtDecoder {

	}

	/**
	 * {@link JwtDecodeCollector} implementation.
	 */
	private class JwtDecodeCollectorImpl implements JwtDecodeCollector {

		@Override
		public JwtDecodeKey[] getCurrentKeys() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setKeys(long timeToNextCheck, JwtDecodeKey... keys) {

		}

		@Override
		public void setFailure(long timeToNextCheck, Throwable cause) {
			// TODO Auto-generated method stub

		}
	}
}