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
