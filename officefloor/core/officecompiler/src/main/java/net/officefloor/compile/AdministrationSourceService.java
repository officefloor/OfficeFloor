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

import net.officefloor.compile.spi.administration.source.AdministrationSource;

/**
 * <p>
 * Service to plug-in an {@link AdministrationSource} {@link Class} alias by
 * including the extension {@link AdministrationSource} jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addAdministrationSourceAlias(String, Class)} will
 * be invoked for each found {@link AdministrationSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceService<E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> {

	/**
	 * Obtains the alias for the {@link AdministrationSource} {@link Class}.
	 * 
	 * @return Alias for the {@link AdministrationSource} {@link Class}.
	 */
	String getAdministrationSourceAlias();

	/**
	 * Obtains the {@link AdministrationSource} {@link Class}.
	 * 
	 * @return {@link AdministrationSource} {@link Class}.
	 */
	Class<S> getAdministrationSourceClass();

}
