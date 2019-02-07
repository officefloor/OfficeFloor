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