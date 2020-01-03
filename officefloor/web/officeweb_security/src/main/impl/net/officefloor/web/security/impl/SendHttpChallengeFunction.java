package net.officefloor.web.security.impl;

import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} to send the {@link HttpChallenge}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendHttpChallengeFunction extends StaticManagedFunction<SendHttpChallengeFunction.Dependencies, None> {

	public static enum Dependencies {
		HTTP_CHALLENGE_CONTEXT, SERVER_HTTP_CONNECTION
	}

	/*
	 * ==================== ManagedFunction =========================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the dependencies
		HttpChallengeContext httpChallengeContext = (HttpChallengeContext) context
				.getObject(Dependencies.HTTP_CHALLENGE_CONTEXT);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);

		// Send the challenge
		HttpChallengeContextManagedObjectSource.loadHttpChallenge(httpChallengeContext, connection.getResponse());
	}

}