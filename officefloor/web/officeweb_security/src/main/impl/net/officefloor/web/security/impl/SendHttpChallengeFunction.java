/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
