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

package net.officefloor.web.security.build;

import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Interface for {@link HttpSecurer} to correspond with the
 * {@link HttpSecurable}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurableBuilder {

	/**
	 * Specifies the particular {@link HttpSecurity}.
	 * 
	 * @param httpSecurityName
	 *            Name of the {@link HttpSecurity} to use.
	 */
	void setHttpSecurityName(String httpSecurityName);

	/**
	 * Adds to listing of roles that must have at least one for access.
	 * 
	 * @param anyRole
	 *            Any role.
	 */
	void addRole(String anyRole);

	/**
	 * Adds to listing of required roles that must have all for access.
	 * 
	 * @param requiredRole
	 *            Required roles.
	 */
	void addRequiredRole(String requiredRole);

}
