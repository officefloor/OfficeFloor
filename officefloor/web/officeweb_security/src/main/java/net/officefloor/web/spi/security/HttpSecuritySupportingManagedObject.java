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

package net.officefloor.web.spi.security;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * <p>
 * {@link HttpSecuritySource} configured {@link ManagedObject} to provide
 * supporting dependencies to the {@link HttpAuthentication} and
 * {@link HttpAccessControl} for using the {@link HttpSecuritySource}.
 * <p>
 * An example is a JWT claims {@link ManagedObject}. The JWT claims will be
 * translated to the respective {@link HttpAuthentication} and
 * {@link HttpAccessControl} (with roles). However, there may be need to access
 * the actual JWT claims object by the application. The JWT claims object can
 * then be made available via {@link HttpSecuritySupportingManagedObject} for
 * dependency injection.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObject<O extends Enum<O>> extends PropertyConfigurable {

	/**
	 * Links the custom authentication for the dependency.
	 * 
	 * @param dependency Dependency key.
	 */
	void linkAuthentication(O dependency);

	/**
	 * Links the {@link HttpAuthentication} for the dependency.
	 * 
	 * @param dependency Dependency key.
	 */
	void linkHttpAuthentication(O dependency);

	/**
	 * Links the custom access control for the dependency.
	 * 
	 * @param dependency Dependency key.
	 */
	void linkAccessControl(O dependency);

	/**
	 * Links the {@link HttpAccessControl} for the dependency.
	 * 
	 * @param dependency Dependency key.
	 */
	void linkHttpAccessControl(O dependency);

	/**
	 * Links the {@link HttpSecuritySupportingManagedObject} for the dependency.
	 * 
	 * @param dependency              Dependency key.
	 * @param supportingManagedObject {@link HttpSecuritySupportingManagedObject} to
	 *                                link as dependency.
	 */
	void linkSupportingManagedObject(O dependency, HttpSecuritySupportingManagedObject<?> supportingManagedObject);

}
