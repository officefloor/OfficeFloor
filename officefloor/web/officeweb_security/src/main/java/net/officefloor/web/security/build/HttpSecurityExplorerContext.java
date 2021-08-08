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

import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Context for the {@link HttpSecurityExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExplorerContext {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource}.
	 */
	HttpSecuritySource<?, ?, ?, ?, ?> getHttpSecuritySource();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<?, ?, ?, ?, ?> getHttpSecurityType();

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link HttpSecurityFlowType}.
	 * 
	 * @param flowType {@link HttpSecurityFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link HttpSecurityFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(HttpSecurityFlowType<?> flowType);

}
