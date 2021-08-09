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

import java.security.Principal;

import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpSecurity} enable access to all roles.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousHttpSecuritySource
		extends AbstractHttpSecuritySource<HttpAuthentication<Void>, HttpAccessControl, Void, None, None>
		implements HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None>,
		HttpAuthentication<Void>, HttpAccessControl {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Anonymous {@link Principal}.
	 */
	private static final Principal anonymous = new Principal() {
		@Override
		public String getName() {
			return "anonymous";
		}
	};

	/*
	 * ===================== HttpSecuritySource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> context)
			throws Exception {
		context.setAuthenticationClass((Class) HttpAuthentication.class);
		context.setAccessControlClass(HttpAccessControl.class);
	}

	@Override
	public HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		return this;
	}

	/*
	 * ======================== HttpSecurity ========================
	 */

	@Override
	public HttpAuthentication<Void> createAuthentication(AuthenticationContext<HttpAccessControl, Void> context) {
		return this;
	}

	@Override
	public boolean ratify(Void credentials, RatifyContext<HttpAccessControl> context) {
		context.accessControlChange(this, null);
		return true;
	}

	@Override
	public void authenticate(Void credentials, AuthenticateContext<HttpAccessControl, None, None> context)
			throws HttpException {
		throw new IllegalStateException(
				"Should always obtain " + HttpAccessControl.class.getSimpleName() + " from ratify");
	}

	@Override
	public void challenge(ChallengeContext<None, None> context) throws HttpException {
		// Never challenges
	}

	@Override
	public void logout(LogoutContext<None, None> context) throws HttpException {
		// Always logged in, so no operation
	}

	/*
	 * ====================== HttpAuthentication ======================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		return true;
	}

	@Override
	public Class<Void> getCredentialsType() {
		return Void.class;
	}

	@Override
	public void authenticate(Void credentials, AuthenticateRequest authenticateRequest) {
		authenticateRequest.authenticateComplete(null);
	}

	@Override
	public HttpAccessControl getAccessControl() {
		return this;
	}

	@Override
	public void logout(LogoutRequest logoutRequest) {
		logoutRequest.logoutComplete(null);
	}

	/*
	 * ====================== HttpAccessControl ======================
	 */

	@Override
	public String getAuthenticationScheme() {
		return "Anonymous";
	}

	@Override
	public Principal getPrincipal() {
		return anonymous;
	}

	@Override
	public boolean inRole(String role) {
		return true;
	}

}
