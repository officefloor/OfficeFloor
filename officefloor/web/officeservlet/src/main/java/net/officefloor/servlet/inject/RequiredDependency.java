/*-
 * #%L
 * Servlet
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
