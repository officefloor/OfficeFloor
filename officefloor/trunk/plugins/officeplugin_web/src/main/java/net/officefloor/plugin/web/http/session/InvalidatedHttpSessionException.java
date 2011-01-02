/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.session;

/**
 * Indicates the {@link HttpSession} is currently invalidated and can not be
 * used. This can occur:
 * <ol>
 * <li>after the {@link HttpSession} has been invalidated with no further
 * {@link HttpSession} required (in other words not creating another
 * {@link HttpSession})</li>
 * <li>during {@link HttpSession} invalidation as another {@link HttpSession} is
 * being created</li>
 * <li>failure in invalidating the {@link HttpSession}</li>
 * </ol>
 *
 * @author Daniel Sagenschneider
 */
public class InvalidatedHttpSessionException extends IllegalStateException {

	/**
	 * Initiate.
	 */
	public InvalidatedHttpSessionException() {
	}

	/**
	 * Initiate with cause.
	 *
	 * @param cause
	 *            Cause of {@link HttpSession} being invalid.
	 */
	public InvalidatedHttpSessionException(Throwable cause) {
		super(cause);
	}

}