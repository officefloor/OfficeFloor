/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.role;

import java.util.Collection;

/**
 * Collects the roles for the JWT claims.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtRoleCollector<C> {

	/**
	 * Obtains the JWT claims.
	 * 
	 * @return JWT claims.
	 */
	C getClaims();

	/**
	 * Specifies the roles.
	 * 
	 * @param roles Roles
	 */
	void setRoles(Collection<String> roles);

	/**
	 * Indicates failure in retrieving the roles.
	 * 
	 * @param cause Cause of failure.
	 */
	void setFailure(Throwable cause);

}
