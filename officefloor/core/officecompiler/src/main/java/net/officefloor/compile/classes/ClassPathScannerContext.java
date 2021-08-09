/*-
 * #%L
 * OfficeCompiler
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
