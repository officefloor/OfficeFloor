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
package net.officefloor.web.security.scheme;

import java.security.Principal;

import net.officefloor.web.security.HttpAccessControl;

/**
 * Mock {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAccessControl implements HttpAccessControl {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link MockAccessControl}.
	 */
	private final MockAccessControl accessControl;

	/**
	 * Instantiate.
	 * 
	 * @param accessControl {@link MockAccessControl}.
	 */
	public MockHttpAccessControl(MockAccessControl accessControl) {
		this.accessControl = accessControl;
	}

	/*
	 * ================= HttpAccessControl ==============
	 */

	@Override
	public String getAuthenticationScheme() {
		return this.accessControl.getAuthenticationScheme();
	}

	@Override
	public Principal getPrincipal() {
		return () -> this.accessControl.getUserName();
	}

	@Override
	public boolean inRole(String role) {
		return this.accessControl.getRoles().contains(role);
	}

}