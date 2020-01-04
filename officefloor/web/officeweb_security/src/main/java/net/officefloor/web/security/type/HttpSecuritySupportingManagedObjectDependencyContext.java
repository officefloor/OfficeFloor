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

package net.officefloor.web.security.type;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * Context for extracting the {@link OfficeManagedObject} for the
 * {@link HttpSecuritySupportingManagedObjectDependencyType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectDependencyContext {

	/**
	 * Obtains the custom authentication.
	 * 
	 * @return Custom authentication.
	 */
	OfficeManagedObject getAuthentication();

	/**
	 * Obtains the {@link HttpAuthentication}.
	 * 
	 * @return {@link HttpAuthentication}.
	 */
	OfficeManagedObject getHttpAuthentication();

	/**
	 * Obtains the custom access control.
	 * 
	 * @return Custom access control.
	 */
	OfficeManagedObject getAccessControl();

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl}.
	 */
	OfficeManagedObject getHttpAccessControl();

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @param supportingManagedObject {@link HttpSecuritySupportingManagedObject}.
	 * @return {@link OfficeManagedObject} for the
	 *         {@link HttpSecuritySupportingManagedObject}.
	 */
	OfficeManagedObject getSupportingManagedObject(HttpSecuritySupportingManagedObject<?> supportingManagedObject);

}
