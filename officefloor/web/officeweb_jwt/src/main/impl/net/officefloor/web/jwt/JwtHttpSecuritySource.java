package net.officefloor.web.jwt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import net.officefloor.frame.api.build.None;
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
	 * {@link KeyHolder} to wait on for initial load.
	 */
	private final CompletableFuture<KeyHolder> initialKeyHolder = new CompletableFuture<>();

	/**
	 * {@link KeyHolder}.
	 */
	private final AtomicReference<KeyHolder> keyHolder = new AtomicReference<>();

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

		// Create poller for JWT keys
		
		
		// Load the decode keys
		context.registerStartupProcess(Flows.RETRIEVE_KEYS, new JwtDecodeCollectorImpl(), (error) -> {
			if (error != null) {
				throw error;
			}
		});
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

		// Obtain the initial keys
		KeyHolder holder = this.keyHolder.get();
		if (holder == null) {

			// Block some time until initial keys are available
			try {
				holder = this.initialKeyHolder.get(1000, TimeUnit.MICROSECONDS);

			} catch (InterruptedException | TimeoutException ex) {
				// Indicate took too long for keys
				context.accessControlChange(null, ex);

			} catch (ExecutionException ex) {
				// Obtain the cause and provide error
				Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
				context.accessControlChange(null, cause);
			}
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