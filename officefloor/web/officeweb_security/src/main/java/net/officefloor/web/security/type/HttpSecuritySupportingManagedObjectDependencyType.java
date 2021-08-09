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
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of the
 * {@link HttpSecuritySupportingManagedObject} dependency.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectDependencyType<O extends Enum<O>> {

	/**
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	O getKey();

	/**
	 * Obtains the {@link OfficeManagedObject} for this dependency.
	 * 
	 * @param context {@link HttpSecuritySupportingManagedObjectDependencyContext}.
	 * @return {@link OfficeManagedObject} for this dependency.
	 */
	OfficeManagedObject getOfficeManagedObject(HttpSecuritySupportingManagedObjectDependencyContext context);

}
