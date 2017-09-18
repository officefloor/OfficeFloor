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
package net.officefloor.plugin.web.http.continuation;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link HttpUrlContinuationDifferentiator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationDifferentiatorImpl implements
		HttpUrlContinuationDifferentiator {

	/**
	 * Application URI path.
	 */
	private final String applicationUriPath;

	/**
	 * Indicates if requires a secure {@link ServerHttpConnection}.
	 */
	private final Boolean isSecure;

	/**
	 * Initiate.
	 * 
	 * @param applicationUriPath
	 *            Application URI path.
	 * @param isSecure
	 *            Indicates if requires a secure {@link ServerHttpConnection}.
	 *            May be <code>null</code> to indicate servicing whether secure
	 *            or not.
	 */
	public HttpUrlContinuationDifferentiatorImpl(String applicationUriPath,
			Boolean isSecure) {
		this.applicationUriPath = applicationUriPath;
		this.isSecure = isSecure;
	}

	/*
	 * =================== HttpUrlContinuationDifferentiator ================
	 */

	@Override
	public String getApplicationUriPath() {
		return this.applicationUriPath;
	}

	@Override
	public Boolean isSecure() {
		return this.isSecure;
	}

}