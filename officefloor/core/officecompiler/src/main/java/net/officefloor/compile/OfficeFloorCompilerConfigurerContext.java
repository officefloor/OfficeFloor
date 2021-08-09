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

package net.officefloor.compile;

/**
 * Context for the {@link OfficeFloorCompilerConfigurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerConfigurerContext {

	/**
	 * Obtains the {@link OfficeFloorCompiler} being configured.
	 * 
	 * @return {@link OfficeFloorCompiler} being configured.
	 */
	OfficeFloorCompiler getOfficeFloorCompiler();

	/**
	 * <p>
	 * Allows specifying another {@link ClassLoader}.
	 * <p>
	 * To ensure {@link Class} compatibility, the input {@link ClassLoader} must be
	 * a child of the current {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * 
	 * @param classLoader {@link ClassLoader} that is child of
	 *                    {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * @throws IllegalArgumentException If not a child.
	 */
	void setClassLoader(ClassLoader classLoader) throws IllegalArgumentException;

}
