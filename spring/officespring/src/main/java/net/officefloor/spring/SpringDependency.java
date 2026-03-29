/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Spring dependency on {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDependency {

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type.
	 */
	public SpringDependency(String qualifier, Class<?> objectType) {
		this.qualifier = qualifier;
		this.objectType = objectType;
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
	 * Obtains the object type.
	 * 
	 * @return Object type.
	 */
	public Class<?> getObjectType() {
		return objectType;
	}

}
