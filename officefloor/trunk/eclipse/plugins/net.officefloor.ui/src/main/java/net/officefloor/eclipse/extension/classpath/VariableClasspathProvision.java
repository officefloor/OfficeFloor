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
 * {@link ClasspathProvision} for a variable.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableClasspathProvision implements ClasspathProvision {

	/**
	 * Name of the variable.
	 */
	private final String variable;

	/**
	 * Path from the variable.
	 */
	private final String path;

	/**
	 * Initiate.
	 * 
	 * @param variable
	 *            Name of the variable.
	 * @param path
	 *            Path from the variable.
	 */
	public VariableClasspathProvision(String variable, String path) {
		this.variable = variable;
		this.path = path;
	}

	/**
	 * Obtains the variable name.
	 * 
	 * @return Variable name.
	 */
	public String getVariable() {
		return this.variable;
	}

	/**
	 * Obtains the path from the variable.
	 * 
	 * @return Path from the variable.
	 */
	public String getPath() {
		return this.path;
	}
}
