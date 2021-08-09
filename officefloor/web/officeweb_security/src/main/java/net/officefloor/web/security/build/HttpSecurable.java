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
