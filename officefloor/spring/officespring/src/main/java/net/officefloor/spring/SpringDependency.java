/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
