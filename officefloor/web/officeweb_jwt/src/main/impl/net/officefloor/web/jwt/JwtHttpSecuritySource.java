package net.officefloor.web.jwt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
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
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
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
	 * <p>
	 * {@link Property} name for the startup timeout in milliseconds.
	 * <p>
	 * This is the time that {@link HttpRequest} instances are held up waiting the
	 * for the initial {@link JwtDecodeKey} instances to be loaded.
	 */
	public static final String PROEPRTY_STARTUP_TIMEOUT = "startup.timeout";

	/**
	 * Default value for {@link #PROEPRTY_STARTUP_TIMEOUT}.
	 */
	public static final long DEFAULT_STARTUP_TIMEOUT = 1 * 1000;

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		RETRIEVE_KEYS, NO_JWT, INVALID_JWT, EXPIRED_JWT
	}

	/**
	 * {@link HttpRequestState} attribute name for the {@link ChallengeReason}.
	 */
	private static final String CHALLENGE_ATTRIBUTE_NAME = "challenge.reason";

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link JwtClaims} {@link JavaType}.
	 */
	private static final JavaType jwtClaimsJavaType = mapper.constructType(JwtClaims.class);

	/**
	 * {@link JwtHeader} {@link JavaType}.
	 */
	private static final JavaType jwtHeaderJavaType = mapper.constructType(JwtHeader.class);

	/**
	 * UTF-8 {@link Charset}.
	 */
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	static {
		// Ensure JSON deserialising is valid
		if (!mapper.canDeserialize(jwtClaimsJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + JwtClaims.class.getSimpleName());
		}
		if (!mapper.canDeserialize(jwtHeaderJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + JwtHeader.class.getSimpleName());
		}
	}

	/**
	 * Start up timeout.
	 */
	private long startupTimeout;

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
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass((Class) JwtHttpAccessControl.class);

		// Provide flow to retrieve keys
		context.addFlow(Flows.RETRIEVE_KEYS, JwtDecodeCollector.class);

		// Provide challenge flows
		context.addFlow(Flows.NO_JWT, null);
		context.addFlow(Flows.INVALID_JWT, null);
		context.addFlow(Flows.EXPIRED_JWT, null);

		// Load the start up timeout
		this.startupTimeout = Long.parseLong(
				securityContext.getProperty(PROEPRTY_STARTUP_TIMEOUT, String.valueOf(DEFAULT_STARTUP_TIMEOUT)));
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
				context.registerStartupProcess(Flows.RETRIEVE_KEYS, new JwtDecodeCollectorImpl(pollContext), callback);
			} else {
				context.invokeProcess(Flows.RETRIEVE_KEYS, new JwtDecodeCollectorImpl(pollContext), delay, callback);
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

			// Flag for potential challenge that no JWT
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
			decoder = this.jwtDecoder.getState(this.startupTimeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			context.accessControlChange(null, new HttpException(HttpStatus.SERVICE_UNAVAILABLE,
					new TimeoutException("Server timed out loading JWT keys")));
			return; // must obtain decoder
		}

		// Obtain the scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getRequest());
		String jwtToken = scheme.getParameters();

		// Split out the JWT
		String[] jwtParts = jwtToken.split("\\.");
		if (jwtParts.length != 3) {
			// Must have head, claims and signature
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		/*
		 * Undertake parsing out JWT claims and validating the signature.
		 * 
		 * Note: order of operations is least expensive to most expensive to reduce load
		 * on the server. This ensures a server under load has best chance to handle CPU
		 * processing in validating JWTs from many HTTP requests.
		 */

		// Obtain the claims
		String claimsRaw = jwtParts[1];
		byte[] claimsBytes = Base64.getUrlDecoder().decode(claimsRaw);
		JwtClaims claims;
		try {
			claims = mapper.readValue(claimsBytes, jwtClaimsJavaType);
		} catch (IOException e) {
			// Must be able to parse claims
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Obtain the current time (in seconds)
		long currentTime = System.currentTimeMillis() / 1000;

		// Ensure valid window
		if ((claims.nbf != null) && (claims.nbf > currentTime)) {
			// JWT not yet active
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Ensure not expired
		if ((claims.exp != null) && (claims.exp < currentTime)) {
			// JWT expired
			this.challenge(ChallengeReason.EXPIRED_JWT, context);
			return;
		}

		// Obtain the signature algorithm
		String headerRaw = jwtParts[0];
		byte[] headerBytes = Base64.getUrlDecoder().decode(headerRaw);
		JwtHeader header;
		try {
			header = mapper.readValue(headerBytes, jwtHeaderJavaType);
		} catch (IOException ex) {
			// Must be able to parse header
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Ensure have algorithm
		if (header.alg == null) {
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// TODO handle algorithm
		System.out.println("TODO: find key with algorithm " + header.alg + " from:");
		for (JwtDecodeKey decodeKey : decoder.getJwtDecodeKeys()) {
			Key key = decodeKey.getKey();
			System.out.println("\t" + key.getAlgorithm() + ", " + key.getFormat() + ", " + key.toString());
		}

		// TODO see if implement (or re-use)
		JwtParser parser = Jwts.parser().setSigningKey(decoder.getJwtDecodeKeys()[0].getKey());
		Jws<Claims> jws = parser.parseClaimsJws(jwtToken);

		// As here successful
		System.out.println("TODO: valid JWT - " + jws.getBody());
	}

	/**
	 * Loads the challenge details.
	 * 
	 * @param reason  {@link ChallengeReason}.
	 * @param context {@link AuthenticateContext}.
	 */
	private void challenge(ChallengeReason reason, AuthenticateContext<JwtHttpAccessControl<C>, None> context) {
		context.getRequestState().setAttribute(context.getQualifiedAttributeName(CHALLENGE_ATTRIBUTE_NAME), reason);
	}

	@Override
	public void challenge(ChallengeContext<None, Flows> context) throws HttpException {

		// Challenge, so unauthorised by default
		HttpResponse response = context.getConnection().getResponse();
		response.setStatus(HttpStatus.UNAUTHORIZED);

		// Determine cause of challenge
		ChallengeReason reason = (ChallengeReason) context.getRequestState()
				.getAttribute(context.getQualifiedAttributeName(CHALLENGE_ATTRIBUTE_NAME));
		switch (reason) {
		case NO_JWT:
			context.doFlow(Flows.NO_JWT);
			break;

		case INVALID_JWT:
			context.doFlow(Flows.INVALID_JWT);
			break;

		case EXPIRED_JWT:
			context.doFlow(Flows.EXPIRED_JWT);
			break;
		}
	}

	@Override
	public void logout(LogoutContext<None> context) throws HttpException {
		// Not able to "logout" JWT token (as typically externally managed)
	}

	/**
	 * Challenge reason.
	 */
	private static enum ChallengeReason {
		NO_JWT, INVALID_JWT, EXPIRED_JWT
	}

	/**
	 * JWT decoder.
	 */
	private static class JwtDecoder {

		/**
		 * {@link JwtDecodeKey} instances.
		 */
		private final JwtDecodeKey[] decodeKeys;

		/**
		 * Instantiate.
		 * 
		 * @param decodeKeys {@link JwtDecodeKey} instances.
		 */
		private JwtDecoder(JwtDecodeKey[] decodeKeys) {
			this.decodeKeys = decodeKeys;
		}

		/**
		 * Obtains the {@link JwtDecodeKey} instances being used.
		 * 
		 * @return {@link JwtDecodeKey} instances being used.
		 */
		private JwtDecodeKey[] getJwtDecodeKeys() {
			return this.decodeKeys;
		}
	}

	/**
	 * {@link JwtDecodeCollector} implementation.
	 */
	private class JwtDecodeCollectorImpl implements JwtDecodeCollector {

		/**
		 * {@link StatePollContext} for the {@link JwtDecoder}.
		 */
		private final StatePollContext<JwtDecoder> context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link StatePollContext} for the {@link JwtDecoder}.
		 */
		private JwtDecodeCollectorImpl(StatePollContext<JwtDecoder> context) {
			this.context = context;
		}

		/*
		 * ============= JwtDecodeCollector ===============
		 */

		@Override
		public JwtDecodeKey[] getCurrentKeys() {
			JwtDecoder decoder = JwtHttpSecuritySource.this.jwtDecoder.getStateNow();
			return decoder != null ? decoder.getJwtDecodeKeys() : null;
		}

		@Override
		public void setKeys(JwtDecodeKey... keys) {
			this.context.setNextState(new JwtDecoder(keys), -1, null);
		}

		@Override
		public void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit) {
			this.context.setFailure(cause, timeToNextCheck, unit);
		}
	}

	/**
	 * JWT header.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JwtHeader {

		/**
		 * Algorithm.
		 */
		private String alg;

		/**
		 * Specifies the algorithm.
		 * 
		 * @param alg Algorithm.
		 */
		public void setAlg(String alg) {
			this.alg = alg;
		}
	}

	/**
	 * JWT claims.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JwtClaims {

		/**
		 * Expiry time.
		 */
		private Long exp;

		/**
		 * Not before time.
		 */
		private Long nbf;

		/**
		 * Specifies expiry time.
		 * 
		 * @param exp Expiry time.
		 */
		public void setExp(Long exp) {
			this.exp = exp;
		}

		/**
		 * Specifies not before time.
		 * 
		 * @param nbf Not before time.
		 */
		public void setNbf(Long nbf) {
			this.nbf = nbf;
		}
	}

}