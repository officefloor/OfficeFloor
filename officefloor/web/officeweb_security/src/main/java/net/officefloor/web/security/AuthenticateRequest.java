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
public interface AuthenticateRequest {

	/**
	 * Notifies the requester that the authenticate attempt has completed.
	 * 
	 * @param failure
	 *            On failure to authenticate it will be the cause of the
	 *            failure. Note that a null {@link Throwable} does not
	 *            necessarily mean authentication was successful (just the
	 *            attempt has complete).
	 */
	void authenticateComplete(Throwable failure);

}
