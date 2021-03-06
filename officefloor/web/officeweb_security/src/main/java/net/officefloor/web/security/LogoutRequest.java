/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security;

/**
 * Request for logging out.
 * 
 * @author Daniel Sagenschneider
 */
public interface LogoutRequest {

	/**
	 * Notifies the requester that the log out has completed.
	 * 
	 * @param failure
	 *            On a successful logout this will be <code>null</code>. On
	 *            failure to logout it will be the cause of the failure.
	 */
	void logoutComplete(Throwable failure);

}
