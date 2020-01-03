package net.officefloor.web.spi.security;

import net.officefloor.server.http.HttpHeader;

/**
 * Context for the {@link HttpChallenge}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpChallengeContext {

	/**
	 * <p>
	 * Sets the {@link HttpChallenge}.
	 * <p>
	 * This should be used instead of directly adding the {@link HttpHeader}, so
	 * that can potentially include multiple {@link HttpChallenge} instances.
	 * 
	 * @param authenticationScheme
	 *            Authentication scheme.
	 * @param realm
	 *            Realm.
	 * @return {@link HttpChallenge}.
	 */
	HttpChallenge setChallenge(String authenticationScheme, String realm);

}