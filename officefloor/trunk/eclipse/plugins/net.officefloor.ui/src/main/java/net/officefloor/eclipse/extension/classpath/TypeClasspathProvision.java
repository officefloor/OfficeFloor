/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.extension.classpath;

/**
 * {@link ClasspathProvision} for a {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeClasspathProvision implements ClasspathProvision {

	/**
	 * {@link Class}.
	 */
	private final Class<?> type;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            {@link Class}.
	 */
	public TypeClasspathProvision(Class<?> type) {
		this.type = type;
	}

	/**
	 * Obtains the {@link Class}.
	 * 
	 * @return {@link Class}.
	 */
	public Class<?> getType() {
		return this.type;
	}
}
