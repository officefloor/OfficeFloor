/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

/**
 * Factory {@link FunctionalInterface} to create the
 * {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface GoogleIdTokenVerifierFactory {

	/**
	 * Creates the {@link GoogleIdTokenVerifier}.
	 *
	 * @return {@link GoogleIdTokenVerifier}.
	 * @throws Exception If fails to create the {@link GoogleIdTokenVerifier}.
	 */
	GoogleIdTokenVerifier create() throws Exception;

}
