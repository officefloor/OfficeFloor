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

import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * <code>Type definition</code> of a dependency required by the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityDependencyType<D extends Enum<D>> {

	/**
	 * Obtains the name of the dependency.
	 * 
	 * @return Name of the dependency.
	 */
	String getDependencyName();

	/**
	 * Obtains the index identifying the dependency.
	 * 
	 * @return Index identifying the dependency.
	 */
	int getIndex();

	/**
	 * Obtains the {@link Class} that the dependent object must
	 * extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getDependencyType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	D getKey();

}
