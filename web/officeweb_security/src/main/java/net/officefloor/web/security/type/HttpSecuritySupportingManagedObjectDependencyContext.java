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
