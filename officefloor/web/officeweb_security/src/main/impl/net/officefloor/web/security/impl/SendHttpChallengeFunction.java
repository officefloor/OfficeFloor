/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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