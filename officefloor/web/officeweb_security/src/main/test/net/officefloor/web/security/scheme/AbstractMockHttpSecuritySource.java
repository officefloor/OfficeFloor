/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.scheme;

import net.officefloor.frame.api.source.TestSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Abstract mock {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class AbstractMockHttpSecuritySource<C, O extends Enum<O>, F extends Enum<F>>
		extends AbstractHttpSecuritySource<MockAuthentication, MockAccessControl, C, O, F>
		implements HttpSecurity<MockAuthentication, MockAccessControl, C, O, F> {

	/*
	 * ==================== HttpSecuritySource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<MockAuthentication, MockAccessControl, C, O, F> context)
			throws Exception {
		context.setAuthenticationClass(MockAuthentication.class);
		context.setHttpAuthenticationFactory((authentication) -> new MockHttpAuthentication<>(authentication, null));
		context.setAccessControlClass(MockAccessControl.class);
		context.setHttpAccessControlFactory((accessControl) -> new MockHttpAccessControl(accessControl));
	}

	@Override
	public HttpSecurity<MockAuthentication, MockAccessControl, C, O, F> sourceHttpSecurity(HttpSecurityContext context)
			throws HttpException {
		return this;
	}

	/*
	 * ======================== HttpSecurity =======================
	 */

	@Override
	public MockAuthentication createAuthentication(AuthenticationContext<MockAccessControl, C> context) {
		MockAuthentication authentication = new MockAuthentication(context);
		context.authenticate(null, null); // attempt authentication
		return authentication;
	}

	@Override
	public boolean ratify(C credentials, RatifyContext<MockAccessControl> context) {
		return true;
	}

	@Override
	public void authenticate(C credentials, AuthenticateContext<MockAccessControl, O, F> context) throws HttpException {
		context.accessControlChange(new MockAccessControl("mock", "test"), null);
	}

	@Override
	public void challenge(ChallengeContext<O, F> context) throws HttpException {
	}

	@Override
	public void logout(LogoutContext<O, F> context) throws HttpException {
	}

}
