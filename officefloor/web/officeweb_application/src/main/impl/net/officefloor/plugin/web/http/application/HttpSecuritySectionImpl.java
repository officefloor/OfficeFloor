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
package net.officefloor.plugin.web.http.application;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.web.http.security.HttpSecurityConfiguration;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * {@link HttpSecuritySource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySectionImpl<S, C, D extends Enum<D>, F extends Enum<F>>
		implements HttpSecurity, HttpSecurityConfiguration<S, C, D, F> {

	/**
	 * Default timeout is 10 seconds.
	 */
	public static final long DEFAULT_HTTP_SECURITY_TIMEOUT = 10 * 1000;

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection section;

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<S, C, D, F> httpSecuritySource;

	/**
	 * {@link PropertyList} to configure the {@link HttpSecuritySource}.
	 */
	private final PropertyList properties;

	/**
	 * Initiate with default timeout.
	 */
	private long securityTimeout = DEFAULT_HTTP_SECURITY_TIMEOUT;

	/**
	 * {@link HttpSecurityType}.
	 */
	private HttpSecurityType<S, C, D, F> httpSecurityType;

	/**
	 * Initiate.
	 * 
	 * @param architect
	 *            {@link OfficeArchitect}.
	 * @param securityName
	 *            Name of the {@link HttpSecurity}.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource} instance.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link HttpSecuritySource}.
	 */
	public HttpSecuritySectionImpl(OfficeArchitect architect, String securityName,
			HttpSecuritySource<S, C, D, F> httpSecuritySource, PropertyList properties) {
		this.httpSecuritySource = httpSecuritySource;
		this.properties = properties;

		// Create the section
		this.section = architect.addOfficeSection(securityName, new HttpSecuritySectionSource(this), null);
	}

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link HttpSecuritySource}.
	 */
	PropertyList getProperties() {
		return this.properties;
	}

	/**
	 * Obtains the time in milliseconds before timing out authentication.
	 * 
	 * @return Time in milliseconds before timing out authentication.
	 */
	long getSecurityTimeout() {
		return this.securityTimeout;
	}

	/**
	 * Specifies the {@link HttpSecurityType}.
	 * 
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 */
	void setHttpSecurityType(HttpSecurityType<S, C, D, F> httpSecurityType) {
		this.httpSecurityType = httpSecurityType;
	}

	/*
	 * ====================== HttpSecuritySection =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public void setSecurityTimeout(long timeout) {
		this.securityTimeout = timeout;
	}

	/*
	 * ====================== PropertyConfigurable ====================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/*
	 * ==================== HttpSecurityConfiguration =================
	 */

	@Override
	public HttpSecuritySource<S, C, D, F> getHttpSecuritySource() {
		return this.httpSecuritySource;
	}

	@Override
	public HttpSecurityType<S, C, D, F> getHttpSecurityType() {
		return this.httpSecurityType;
	}

}