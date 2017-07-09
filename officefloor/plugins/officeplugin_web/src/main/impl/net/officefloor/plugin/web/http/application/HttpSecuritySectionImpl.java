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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * {@link HttpSecuritySource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySectionImpl implements HttpSecuritySection {

	/**
	 * Default timeout is 10 seconds.
	 */
	public static final long DEFAULT_HTTP_SECURITY_TIMEOUT = 10 * 1000;

	/**
	 * {@link Class} of the {@link HttpSecuritySource}.
	 */
	private final Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass;

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection section;

	/**
	 * Initiate with default timeout.
	 */
	private long securityTimeout = DEFAULT_HTTP_SECURITY_TIMEOUT;

	/**
	 * Initiate.
	 * 
	 * @param section
	 *            {@link OfficeSection}.
	 * @param httpSecuritySourceClass
	 *            {@link HttpSecuritySource} class.
	 */
	public HttpSecuritySectionImpl(OfficeSection section,
			Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass) {
		this.section = section;
		this.httpSecuritySourceClass = httpSecuritySourceClass;
	}

	/*
	 * ====================== HttpSecurityAutoWireSection =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public Class<? extends HttpSecuritySource<?, ?, ?, ?>> getHttpSecuritySourceClass() {
		return this.httpSecuritySourceClass;
	}

	@Override
	public long getSecurityTimeout() {
		return this.securityTimeout;
	}

	@Override
	public void setSecurityTimeout(long timeout) {
		this.securityTimeout = timeout;
	}

}