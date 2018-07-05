/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * <p>
 * {@link Escalation} indicating authentication is required.
 * <p>
 * This may be thrown by any functionality as the {@link WebArchitect} is
 * expected to catch this {@link Escalation} at the {@link Office} level and
 * issue a challenge to the client.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationRequiredException extends RuntimeException {

	/**
	 * Name of the required {@link HttpSecurity}.
	 */
	private final String requiredHttpSecurityName;

	/**
	 * Initiate for any {@link HttpSecurity}.
	 */
	public AuthenticationRequiredException() {
		this(null);
	}

	/**
	 * Initiate for a specific {@link HttpSecurity}.
	 * 
	 * @param requiredHttpSecurityName
	 *            Name of the specific {@link HttpSecurity} required. May be
	 *            <code>null</code> to indicate any configured
	 *            {@link HttpSecurity}.
	 */
	public AuthenticationRequiredException(String requiredHttpSecurityName) {
		this.requiredHttpSecurityName = requiredHttpSecurityName;
	}

	/**
	 * Obtains the required {@link HttpSecurity} name.
	 * 
	 * @return Required {@link HttpSecurity} name or <code>null</code> if any
	 *         {@link HttpSecurity}.
	 */
	public String getRequiredHttpSecurityName() {
		return this.requiredHttpSecurityName;
	}

}