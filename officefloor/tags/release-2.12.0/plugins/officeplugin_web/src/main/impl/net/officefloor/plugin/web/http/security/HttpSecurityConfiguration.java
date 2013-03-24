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

import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * {@link HttpSecurity} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityConfiguration<S, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<S, C, D, F> httpSecuritySource;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<S, C, D, F> httpSecurityType;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 */
	public HttpSecurityConfiguration(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			HttpSecurityType<S, C, D, F> httpSecurityType) {
		this.httpSecuritySource = httpSecuritySource;
		this.httpSecurityType = httpSecurityType;
	}

	/**
	 * Obtains the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource}.
	 */
	public HttpSecuritySource<S, C, D, F> getHttpSecuritySource() {
		return this.httpSecuritySource;
	}

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	public HttpSecurityType<S, C, D, F> getHttpSecurityType() {
		return this.httpSecurityType;
	}

}