/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security.scheme;

import java.io.IOException;

import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.impl.AbstractHttpSecuritySource;

/**
 * Form based {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpSecuritySource
		extends
		AbstractHttpSecuritySource<HttpSecurity, HttpCredentials, FormHttpSecuritySource.Dependencies, FormHttpSecuritySource.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		CREDENTIAL_STORE
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		FORM_LOGIN_PAGE
	}

	/**
	 * Name of property to retrieve the realm being secured.
	 */
	public static final String PROPERTY_REALM = "http.security.form.realm";

	/*
	 * ======================== HttpSecuritySource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractHttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.loadSpecification");
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HttpSecurity, HttpCredentials, Dependencies, Flows> context)
			throws Exception {
		// TODO implement
		// AbstractHttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.loadMetaData");
	}

	@Override
	public boolean ratify(
			HttpRatifyContext<HttpSecurity, HttpCredentials> context) {
		// TODO implement
		// HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.ratify
		throw new UnsupportedOperationException(
				"TODO implement HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.ratify");
	}

	@Override
	public void authenticate(
			HttpAuthenticateContext<HttpSecurity, HttpCredentials, Dependencies> context)
			throws IOException {
		// TODO implement
		// HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.authenticate
		throw new UnsupportedOperationException(
				"TODO implement HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.authenticate");
	}

	@Override
	public void challenge(HttpChallengeContext<Dependencies, Flows> context)
			throws IOException {
		// TODO implement
		// HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.challenge
		throw new UnsupportedOperationException(
				"TODO implement HttpSecuritySource<HttpSecurity,HttpCredentials,Dependencies,Flows>.challenge");
	}

}