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
package net.officefloor.plugin.web.http.security;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Work} and {@link WorkFactory} for {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityWork implements Work, WorkFactory<HttpSecurityWork> {

	/**
	 * {@link HttpSession} attribute for the challenge request state.
	 */
	public static final String ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO = "CHALLENGE_REQUEST_MOMENTO";

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<?, ?, ?, ?> httpSecuritySource;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public HttpSecurityWork(HttpSecuritySource<?, ?, ?, ?> httpSecuritySource) {
		this.httpSecuritySource = httpSecuritySource;
	}

	/**
	 * Obtains the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource}.
	 */
	public HttpSecuritySource<?, ?, ?, ?> getHttpSecuritySource() {
		return this.httpSecuritySource;
	}

	/*
	 * =============== WorkFactory ==========================
	 */

	@Override
	public HttpSecurityWork createWork() {
		return this;
	}

}