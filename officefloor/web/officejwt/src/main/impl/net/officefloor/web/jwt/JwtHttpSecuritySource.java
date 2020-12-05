/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.jwt;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import io.jsonwebtoken.io.Decoder;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.poll.StatePollContext;
import net.officefloor.plugin.managedobject.poll.StatePoller;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.test.system.AbstractExternalOverride.ContextRunnable;
import net.officefloor.web.jwt.JwtClaimsManagedObjectSource.Dependencies;
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.scheme.HttpAccessControlImpl;
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
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;
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
	 * Allows overriding the creation of {@link JwtValidateKey} instances.
	 */
	@FunctionalInterface
	public static interface JwtValidateKeysFactory {

		/**
		 * Obtains the {@link JwtValidateKey} instances to use.
		 * 
		 * @return {@link JwtValidateKey} instances to use.
		 * @throws Exception If fails to create the {@link JwtValidateKey} instances.
		 */
		JwtValidateKey[] createJwtValidateKeys() throws Exception;
	}

	/**
	 * <p>
	 * Uses the {@link JwtValidateKeysFactory} for keys.
	 * <p>
	 * This is typically used for testing to allow overriding the
	 * {@link JwtValidateKey} instances being used.
	 * 
	 * @param validateKeysFactory {@link JwtValidateKeysFactory}. May be
	 *                            <code>null</code> to not override.
	 * @throws T If failure in {@link ContextRunnable}.
	 */
	public static void setOverrideKeys(JwtValidateKeysFactory validateKeysFactory) {
		if (validateKeysFactory != null) {
			// Override the keys
			threadLocalKeysOverride.set(new JwtKeysFactoryOverride(validateKeysFactory));
		} else {
			// Clear the override
			threadLocalKeysOverride.remove();
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link JwtKeysFactoryOverride}.
	 */
	private static ThreadLocal<JwtKeysFactoryOverride> threadLocalKeysOverride = new ThreadLocal<>();

	/**
	 * Possible override for creation of {@link JwtValidateKey} instances.
	 */
	private static class JwtKeysFactoryOverride {

		/**
		 * {@link JwtValidateKeysFactory}.
		 */
		private final JwtValidateKeysFactory validateKeysFactory;

		/**
		 * Instantiate.
		 * 
		 * @param validateKeysFactory {@link JwtValidateKeysFactory}.
		 */
		private JwtKeysFactoryOverride(JwtValidateKeysFactory validateKeysFactory) {
			this.validateKeysFactory = validateKeysFactory;
		}
	}

	/**
	 * Authentication scheme Bearer.
	 */
	public static final String AUTHENTICATION_SCHEME_BEARER = "Bearer";

	/**
	 * {@link Property} name for the claims {@link Class} to be loaded with claim
	 * information of JWT.
	 */
	public static final String PROPERTY_CLAIMS_CLASS = "claims.class";

	/**
	 * <p>
	 * {@link Property} name for the startup timeout in milliseconds.
	 * <p>
	 * This is the time that {@link HttpRequest} instances are held up waiting the
	 * for the initial {@link JwtValidateKey} instances to be loaded.
	 */
	public static final String PROEPRTY_STARTUP_TIMEOUT = "startup.timeout";

	/**
	 * Default value for {@link #PROEPRTY_STARTUP_TIMEOUT}.
	 */
	public static final long DEFAULT_STARTUP_TIMEOUT = 1 * 1000;

	/**
	 * {@link Property} name for the clock skew in seconds.
	 */
	public static final String PROPERTY_CLOCK_SKEW = "clock.skew";

	/**
	 * Default value for {@link #PROPERTY_CLOCK_SKEW}.
	 */
	public static final long DEFAULT_CLOCK_SKEW = 2;

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		RETRIEVE_KEYS, RETRIEVE_ROLES, NO_JWT, INVALID_JWT, EXPIRED_JWT
	}

	/**
	 * {@link HttpRequestState} attribute name for the {@link ChallengeReason}.
	 */
	private static final String CHALLENGE_ATTRIBUTE_NAME = "challenge.reason";

	/**
	 * Base64 {@link Decoder}.
	 */
	private Decoder<String, byte[]> base64UrlDecoder = (text) -> Base64.getUrlDecoder().decode(text);

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

	static {
		// Ensure JSON deserialising is valid
		if (!mapper.canDeserialize(jwtClaimsJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + JwtClaims.class.getSimpleName());
		}
		if (!mapper.canDeserialize(jwtHeaderJavaType)) {
			throw new IllegalStateException("Unable to deserialize " + JwtHeader.class.getSimpleName());
		}

		// Ensure ignore unknown properties (avoid added "exp" causing problems)
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * {@link Clock}.
	 */
	private Clock<Long> clock;

	/**
	 * Skew in seconds for {@link Clock} time to coordinate with JWT authority.
	 */
	private long clockSkew;

	/**
	 * Claims {@link Class}.
	 */
	private Class<?> claimsClass;

	/**
	 * Claims {@link Class} {@link JavaType}.
	 */
	private JavaType claimsJavaType;

	/**
	 * Start up timeout.
	 */
	private long startupTimeout;

	/**
	 * {@link StatePoller} to keep the {@link JwtValidateKey} instances up to date.
	 */
	private StatePoller<JwtValidateKey[], None> jwtValidateKeys;

	/**
	 * {@link JwtKeysFactoryOverride}. Will be <code>null</code> if not overriding.
	 */
	private JwtKeysFactoryOverride keysOverride;

	/*
	 * ==================== HttpSecuritySource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLAIMS_CLASS, "Claims Class");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(
			MetaDataContext<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> context)
			throws Exception {
		HttpSecuritySourceContext securityContext = context.getHttpSecuritySourceContext();

		// Load the configuration
		this.clock = securityContext.getClock((time) -> time);
		this.clockSkew = Long
				.parseLong(securityContext.getProperty(PROPERTY_CLOCK_SKEW, String.valueOf(DEFAULT_CLOCK_SKEW)));
		this.claimsClass = securityContext.loadClass(securityContext.getProperty(PROPERTY_CLAIMS_CLASS));
		this.startupTimeout = Long.parseLong(
				securityContext.getProperty(PROEPRTY_STARTUP_TIMEOUT, String.valueOf(DEFAULT_STARTUP_TIMEOUT)));

		// Ensure claims class can be deserialised
		this.claimsJavaType = mapper.constructType(this.claimsClass);
		if (!mapper.canDeserialize(this.claimsJavaType)) {
			throw new IOException("Unable to deserialise " + this.claimsClass.getName() + " to load JWT claims");
		}

		// Load the possible JWT keys override
		this.keysOverride = threadLocalKeysOverride.get();
		if ((this.keysOverride == null) || (this.keysOverride.validateKeysFactory == null)) {
			// Only override if have key factory
			this.keysOverride = null;
		}

		// Load the meta-data
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass((Class) JwtHttpAccessControl.class);

		// Provide flow to retrieve keys and obtain roles
		context.addFlow(Flows.RETRIEVE_KEYS, JwtValidateKeyCollector.class);
		context.addFlow(Flows.RETRIEVE_ROLES, JwtRoleCollector.class);

		// Provide challenge flows
		context.addFlow(Flows.NO_JWT, null);
		context.addFlow(Flows.INVALID_JWT, null);
		context.addFlow(Flows.EXPIRED_JWT, null);

		// Provide the JWT claims
		HttpSecuritySupportingManagedObject<Dependencies> jwtClaims = securityContext.addSupportingManagedObject(
				"JWT_CLAIMS", new JwtClaimsManagedObjectSource(this.claimsClass), ManagedObjectScope.THREAD);
		jwtClaims.linkAccessControl(Dependencies.ACCESS_CONTROL);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, JwtHttpAccessControl<C>, Void, None, Flows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		return this;
	}

	@Override
	public void start(HttpSecurityExecuteContext<Flows> context) throws Exception {

		// Only poll if not overriding JWT keys
		if (this.keysOverride != null) {
			return; // override so do not poll for keys
		}

		// Create poller for JWT decoder
		this.jwtValidateKeys = StatePoller.builder(JwtValidateKey[].class, this.clock, (pollContext, callback) -> {
			ManagedObjectStartupProcess startup = context.registerStartupProcess(Flows.RETRIEVE_KEYS,
					new JwtValidateKeyCollectorImpl(pollContext), callback);

			// Run concurrently (may wait on local JWT Authority initialised later)
			startup.setConcurrent(true);
		}, (delay, pollContext, callback) -> {
			context.invokeProcess(Flows.RETRIEVE_KEYS, new JwtValidateKeyCollectorImpl(pollContext), delay, callback);
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
		if ((scheme == null) || (!(AUTHENTICATION_SCHEME_BEARER.equalsIgnoreCase(scheme.getAuthentiationScheme())))) {

			// Flag for potential challenge that no JWT
			context.getRequestState().setAttribute(context.getQualifiedAttributeName(CHALLENGE_ATTRIBUTE_NAME),
					ChallengeReason.NO_JWT);
			return false; // no JWT
		}

		// Has JWT so enough information to authenticate
		return true;
	}

	@Override
	public void authenticate(Void credentials, AuthenticateContext<JwtHttpAccessControl<C>, None, Flows> context)
			throws HttpException {

		// Obtain the JWT validate keys (allowing time to intialise)
		JwtValidateKey[] validateKeys;
		if (this.keysOverride != null) {
			// Override the keys
			try {
				validateKeys = this.keysOverride.validateKeysFactory.createJwtValidateKeys();
			} catch (Exception ex) {
				context.accessControlChange(null, new HttpException(HttpStatus.SERVICE_UNAVAILABLE, ex));
				return; // must obtain validate keys
			}

		} else {
			// Use polled keys
			try {
				validateKeys = this.jwtValidateKeys.getState(this.startupTimeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException ex) {
				context.accessControlChange(null, new HttpException(HttpStatus.SERVICE_UNAVAILABLE,
						new TimeoutException("Server timed out loading JWT keys")));
				return; // must obtain validate keys
			}
		}

		// Obtain the scheme
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(context.getConnection().getRequest());
		String jwtToken = scheme.getParameters();

		// Split out the JWT
		String[] jwtParts = jwtToken.split("\\.");
		if (jwtParts.length != 3) {
			// Must have header, claims and signature
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Obtain the parts
		String headerBase64 = jwtParts[0];
		String claimsBase64 = jwtParts[1];
		String signatureBase64 = jwtParts[2];

		/*
		 * Undertake parsing out JWT claims and validating the signature.
		 * 
		 * Note: order of operations is least expensive to most expensive to reduce load
		 * on the server. This ensures a server under load has best chance to handle CPU
		 * processing in validating JWTs from many HTTP requests.
		 */

		// Obtain the claims
		byte[] claimsBytes = base64UrlDecoder.decode(claimsBase64);
		JwtClaims validateClaims;
		try {
			validateClaims = mapper.readValue(claimsBytes, jwtClaimsJavaType);
		} catch (IOException e) {
			// Must be able to parse claims
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Obtain the current time (in seconds)
		long currentTime = this.clock.getTime();

		// Ensure valid window (taking into account clock skew)
		// Note: signature will only confirm not yet available
		if ((validateClaims.nbf != null) && (validateClaims.nbf > (currentTime + this.clockSkew))) {
			// JWT not yet active
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Ensure not expired (taking into account clock skew)
		// Note: signature will only confirm expired
		if ((validateClaims.exp != null) && (validateClaims.exp < (currentTime - this.clockSkew))) {
			// JWT expired
			this.challenge(ChallengeReason.EXPIRED_JWT, context);
			return;
		}

		// Obtain the signature algorithm
		byte[] headerBytes = base64UrlDecoder.decode(headerBase64);
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

		// Obtain the algorithm
		SignatureAlgorithm algorithm = SignatureAlgorithm.valueOf(header.alg);
		if ((algorithm == null) || (algorithm == SignatureAlgorithm.NONE)) {
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Obtain the JWT without signature
		String jwtWithoutSignature = jwtToken.substring(0,
				headerBase64.length() + ".".length() + claimsBase64.length());

		// Loop over decode keys to determine if JWT valid
		boolean isValid = false;
		NEXT_DECODE_KEY: for (JwtValidateKey decodeKey : validateKeys) {

			// Ensure key is still within window (taking into account clock skew)
			if ((decodeKey.getStartTime() > (currentTime + this.clockSkew))
					|| (decodeKey.getExpireTime() < (currentTime - this.clockSkew))) {
				continue NEXT_DECODE_KEY; // decode key now outside window
			}

			// Attempt to validate the signature
			DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(algorithm, decodeKey.getKey(),
					base64UrlDecoder);
			try {
				if (validator.isValid(jwtWithoutSignature, signatureBase64)) {
					isValid = true;
					break NEXT_DECODE_KEY; // is valid, so no further processing
				}
			} catch (Exception ex) {
				// Ignore as signature not valid
			}
		}
		if (!isValid) {
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Load the claims object for application
		C claims;
		try {
			claims = mapper.readValue(claimsBytes, this.claimsJavaType);
		} catch (IOException ex) {
			// Must be able to parse claims
			this.challenge(ChallengeReason.INVALID_JWT, context);
			return;
		}

		// Retrieve the roles
		String authenticationScheme = scheme.getAuthentiationScheme();
		String principalName = validateClaims.sub;
		JwtRoleCollectorImpl rolesCollector = new JwtRoleCollectorImpl(claims, authenticationScheme, principalName,
				context);
		context.doFlow(Flows.RETRIEVE_ROLES, rolesCollector, rolesCollector);
	}

	/**
	 * Loads the challenge details.
	 * 
	 * @param reason  {@link ChallengeReason}.
	 * @param context {@link AuthenticateContext}.
	 */
	private void challenge(ChallengeReason reason, AuthenticateContext<JwtHttpAccessControl<C>, None, Flows> context) {
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
			context.doFlow(Flows.NO_JWT, null, null);
			break;

		case INVALID_JWT:
			context.doFlow(Flows.INVALID_JWT, null, null);
			break;

		case EXPIRED_JWT:
			context.doFlow(Flows.EXPIRED_JWT, null, null);
			break;
		}
	}

	@Override
	public void logout(LogoutContext<None, Flows> context) throws HttpException {
		// Not able to "logout" JWT token (as typically externally managed)
	}

	/**
	 * Challenge reason.
	 */
	private static enum ChallengeReason {
		NO_JWT, INVALID_JWT, EXPIRED_JWT
	}

	/**
	 * {@link JwtValidateKeyCollector} implementation.
	 */
	private class JwtValidateKeyCollectorImpl implements JwtValidateKeyCollector {

		/**
		 * {@link StatePollContext} for the {@link JwtValidateKey} instances.
		 */
		private final StatePollContext<JwtValidateKey[]> context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link StatePollContext} for the {@link JwtValidateKey}
		 *                instances.
		 */
		private JwtValidateKeyCollectorImpl(StatePollContext<JwtValidateKey[]> context) {
			this.context = context;
		}

		/*
		 * ============= JwtDecodeCollector ===============
		 */

		@Override
		public JwtValidateKey[] getCurrentKeys() {
			return JwtHttpSecuritySource.this.jwtValidateKeys.getStateNow();
		}

		@Override
		public void setKeys(JwtValidateKey... keys) {

			// Filter the keys (also make copy so can not alter)
			List<JwtValidateKey> copy = new ArrayList<>(keys.length);
			NEXT_KEY: for (JwtValidateKey key : keys) {

				// Ignore if null
				if (key == null) {
					continue NEXT_KEY;
				}

				// As here valid, so include decode key
				copy.add(key);
			}

			// Load the JWT decode keys
			JwtValidateKey[] validKeys = copy.toArray(new JwtValidateKey[copy.size()]);
			this.context.setNextState(validKeys, -1, null);
		}

		@Override
		public void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit) {
			this.context.setFailure(cause, timeToNextCheck, unit);
		}
	}

	/**
	 * {@link JwtRoleCollector} implementation.
	 */
	private class JwtRoleCollectorImpl implements JwtRoleCollector<C>, FlowCallback {

		/**
		 * Claims.
		 */
		private final C claims;

		/**
		 * Authentication scheme.
		 */
		private final String authenticationScheme;

		/**
		 * {@link Principal} name.
		 */
		private final String principalName;

		/**
		 * {@link AuthenticateContext}.
		 */
		private final AuthenticateContext<JwtHttpAccessControl<C>, None, Flows> authenticateContext;

		/**
		 * Indicates if completed.
		 */
		private volatile boolean isComplete = false;

		/**
		 * Instantiate.
		 * 
		 * @param claims               Claims.
		 * @param authenticationScheme Authentication scheme.
		 * @param principalName        {@link Principal} name.
		 * @param authenticateContext  {@link AuthenticateContext}.
		 */
		private JwtRoleCollectorImpl(C claims, String authenticationScheme, String principalName,
				AuthenticateContext<JwtHttpAccessControl<C>, None, Flows> authenticateContext) {
			this.claims = claims;
			this.authenticationScheme = authenticationScheme;
			this.principalName = principalName;
			this.authenticateContext = authenticateContext;
		}

		/*
		 * =============== JwtRoleCollector =================
		 */

		@Override
		public C getClaims() {
			return this.claims;
		}

		@Override
		public void setRoles(Collection<String> roles) {

			// Determine if complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Create copy of roles (to ensure serialisable)
			Set<String> rolesSet = new HashSet<>(roles);

			// Create the Jwt HttpAccess
			JwtHttpAccessControl<C> accessControl = new JwtHttpAccessControlImpl(this.authenticationScheme,
					this.principalName, this.claims, rolesSet);
			this.authenticateContext.accessControlChange(accessControl, null);

		}

		@Override
		public void setFailure(Throwable cause) {

			// Determine if complete
			if (this.isComplete) {
				return;
			}
			this.isComplete = true;

			// Flag the failure
			this.authenticateContext.accessControlChange(null, cause);
		}

		/*
		 * ================ FlowCallback ====================
		 */

		@Override
		public void run(Throwable escalation) throws Throwable {

			// Determine if already complete
			if (this.isComplete) {
				return;
			}

			// Ensure have escalation
			if (escalation == null) {
				escalation = new HttpException(HttpStatus.FORBIDDEN,
						new IllegalStateException("No roles loaded for JWT claims"));
			}

			// Indicate failure to load roles
			this.authenticateContext.accessControlChange(null, escalation);
		}
	}

	/**
	 * {@link JwtHttpAccessControl} implementation.
	 */
	private class JwtHttpAccessControlImpl extends HttpAccessControlImpl implements JwtHttpAccessControl<C> {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Claims.
		 */
		private final C claims;

		/**
		 * Instantiate.
		 * 
		 * @param authenticationScheme Authentication scheme.
		 * @param principalName        {@link Principal} name.
		 * @param claims               Claims.
		 * @param roles                Roles.
		 */
		public JwtHttpAccessControlImpl(String authenticationScheme, String principalName, C claims,
				Set<String> roles) {
			super(authenticationScheme, principalName, roles);
			this.claims = claims;
		}

		/*
		 * ================ JwtHttpAccessControl =================
		 */

		@Override
		public C getClaims() {
			return this.claims;
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
		 * Subject.
		 */
		private String sub;

		/**
		 * Expiry time.
		 */
		private Long exp;

		/**
		 * Not before time.
		 */
		private Long nbf;

		/**
		 * Specifies the subject.
		 * 
		 * @param sub Subject.
		 */
		public void setSub(String sub) {
			this.sub = sub;
		}

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
