/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

/**
 * Required dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class RequiredDependency {

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final Class<?> type;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 */
	public RequiredDependency(String qualifier, Class<?> type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * Obtains the type.
	 * 
	 * @return Type.
	 */
	public Class<?> getType() {
		return type;
	}

}
