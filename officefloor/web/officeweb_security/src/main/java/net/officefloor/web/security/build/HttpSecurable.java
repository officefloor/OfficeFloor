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
 * Securable HTTP item.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurable {

	/**
	 * Obtains the name for the {@link HttpSecurity} to use. May be
	 * <code>null</code> if generic {@link HttpSecurity}.
	 * 
	 * @return Name of {@link HttpSecurity} or <code>null</code> for generic
	 *         {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * <p>
	 * Obtains the list of roles that must have at least one for access.
	 * <p>
	 * Empty/<code>null</code> list means needs only be authenticated.
	 * 
	 * @return List of any roles.
	 */
	String[] getAnyRoles();

	/**
	 * <p>
	 * Obtains the list of roles that must have all for access.
	 * <p>
	 * Empty/<code>null</code> list means needs only be authenticated.
	 * 
	 * @return List of required roles.
	 */
	String[] getRequiredRoles();

}
