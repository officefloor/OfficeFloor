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

package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configuration for {@link HttpSecuritySectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityConfiguration<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecurity}.
	 * 
	 * @return {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> getHttpSecurity();

	/**
	 * Obtains the {@link Flow} key {@link Enum} {@link Class}.
	 * 
	 * @return {@link Flow} key {@link Enum} {@link Class}.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<A, AC, C, O, F> getHttpSecurityType();

}
