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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.web.http.route.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;

/**
 * <p>
 * {@link Escalation} indicating authentication is required.
 * <p>
 * This may be thrown by any functionality as the {@link WebArchitect}
 * is expected to catch this {@link Escalation} at the {@link Office} level and
 * issue a challenge to the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationRequiredException extends Exception {

	/**
	 * Indicates whether to save request for {@link HttpUrlContinuation} after
	 * authentication completes.
	 */
	private final boolean isSaveRequest;

	/**
	 * Initiate.
	 */
	public HttpAuthenticationRequiredException() {
		this(true);
	}

	/**
	 * <p>
	 * Used internally to trigger a challenge again.
	 * <p>
	 * Allows not saving the request so that the original request triggering
	 * authentication is used once authentication completes.
	 * 
	 * @param isSaveRequest
	 *            Indicates whether to save request for
	 *            {@link HttpUrlContinuation} after authentication completes.
	 */
	HttpAuthenticationRequiredException(boolean isSaveRequest) {
		this.isSaveRequest = isSaveRequest;
	}

	/**
	 * Indicates whether to save the request.
	 * 
	 * @return <code>true</code> to save the request.
	 */
	public boolean isSaveRequest() {
		return this.isSaveRequest;
	}

}