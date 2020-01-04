/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.classes;

/**
 * Context for the {@link ClassPathScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassPathScannerContext {

	/**
	 * Obtains the name of the {@link Package} to scan for entries.
	 * 
	 * @return Name of the {@link Package} to scan for entries.
	 */
	String getPackageName();

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

	/**
	 * Adds a {@link Class} path entry for the package.
	 * 
	 * @param entryPath {@link Class} path entry for the package. This is required
	 *                  to be full path to the entry.
	 */
	void addEntry(String entryPath);

}
