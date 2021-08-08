/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
