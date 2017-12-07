/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.security.type;

import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link HttpSecurityConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityConfigurationImpl<A, AC, C, O extends Enum<O>, F extends Enum<F>>
		implements HttpSecurityConfiguration<A, AC, C, O, F> {

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<A, AC, C, O, F> security;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> accessControlFactory;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<A, AC, C, O, F> type;

	/**
	 * Instantiate.
	 * 
	 * @param security
	 *            {@link HttpSecurity}.
	 * @param accessControlFactory
	 *            {@link HttpAccessControlFactory}.
	 * @param type
	 *            {@link HttpSecurityType}.
	 */
	public HttpSecurityConfigurationImpl(HttpSecurity<A, AC, C, O, F> security,
			HttpAccessControlFactory<AC> accessControlFactory, HttpSecurityType<A, AC, C, O, F> type) {
		this.security = security;
		this.accessControlFactory = accessControlFactory;
		this.type = type;
	}

	/*
	 * =============== HttpSecurityConfiguration ==================
	 */

	@Override
	public HttpSecurity<A, AC, C, O, F> getHttpSecurity() {
		return this.security;
	}

	@Override
	public HttpAccessControlFactory<AC> getAccessControlFactory() {
		return this.accessControlFactory;
	}

	@Override
	public HttpSecurityType<A, AC, C, O, F> getHttpSecurityType() {
		return this.type;
	}

}