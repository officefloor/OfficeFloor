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

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link HttpUrlContinuationAnnotation} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationAnnotationImpl implements HttpUrlContinuationAnnotation {

	/**
	 * {@link HttpMethod}.
	 */
	private final HttpMethod method;

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
	 * @param method
	 *            {@link HttpMethod}.
	 * @param applicationUriPath
	 *            Application URI path.
	 * @param isSecure
	 *            Indicates if requires a secure {@link ServerHttpConnection}.
	 *            May be <code>null</code> to indicate servicing whether secure
	 *            or not.
	 */
	public HttpUrlContinuationAnnotationImpl(HttpMethod method, String applicationUriPath, Boolean isSecure) {
		this.applicationUriPath = applicationUriPath;
		this.method = method;
		this.isSecure = isSecure;
	}

	/*
	 * =================== HttpUrlContinuationAnnotation ================
	 */

	@Override
	public HttpMethod getHttpMethod() {
		return this.method;
	}

	@Override
	public String getApplicationUriPath() {
		return this.applicationUriPath;
	}

	@Override
	public Boolean isSecure() {
		return this.isSecure;
	}

}