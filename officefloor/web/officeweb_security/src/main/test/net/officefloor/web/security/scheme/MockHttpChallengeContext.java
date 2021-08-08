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
