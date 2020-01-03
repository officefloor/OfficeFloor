package net.officefloor.web.security.scheme;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link ChallengeContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpChallengeContext<O extends Enum<O>, F extends Enum<F>>
		extends AbstractMockHttpSecurityActionContext<O, F> implements ChallengeContext<O, F>, HttpChallenge {

	/**
	 * Challenge.
	 */
	private final StringBuilder challenge = new StringBuilder();

	/**
	 * Initiate.
	 *
	 * @param connection {@link ServerHttpConnection}.
	 */
	public MockHttpChallengeContext(ServerHttpConnection connection) {
		super(connection);
	}

	/**
	 * Initiate.
	 */
	public MockHttpChallengeContext() {
	}

	/**
	 * Obtains the <code>WWW-Authenticate</code> challenge.
	 * 
	 * @return Challenge.
	 */
	public String getChallenge() {
		return this.challenge.toString();
	}

	/*
	 * =================== HttpChallengeContext =====================
	 */

	@Override
	public HttpChallenge setChallenge(String authenticationScheme, String realm) {
		this.challenge.append(authenticationScheme + " realm=\"" + realm + "\"");
		return this;
	}

	/*
	 * ===================== HttpChallenge ===========================
	 */

	@Override
	public void addParameter(String name, String value) {
		this.challenge.append(", " + name + "=" + value);
	}

}