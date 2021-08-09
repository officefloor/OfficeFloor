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

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;

/**
 * <p>
 * Service to plug-in an {@link ManagedFunctionSource} {@link Class} alias by
 * including the extension {@link ManagedFunctionSource} jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addManagedFunctionSourceAlias(String, Class)} will
 * be invoked for each found {@link ManagedFunctionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionSourceService<S extends ManagedFunctionSource> {

	/**
	 * Obtains the alias for the {@link ManagedFunctionSource} {@link Class}.
	 * 
	 * @return Alias for the {@link ManagedFunctionSource} {@link Class}.
	 */
	String getManagedFunctionSourceAlias();

	/**
	 * Obtains the {@link ManagedFunctionSource} {@link Class}.
	 * 
	 * @return {@link ManagedFunctionSource} {@link Class}.
	 */
	Class<S> getManagedFunctionSourceClass();

}
