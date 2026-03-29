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

import net.officefloor.compile.spi.governance.source.GovernanceSource;

/**
 * <p>
 * Service to plug-in an {@link GovernanceSource} {@link Class} alias by
 * including the extension {@link GovernanceSource} jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addGovernanceSourceAlias(String, Class)} will be
 * invoked for each found {@link GovernanceSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceService<I, F extends Enum<F>, S extends GovernanceSource<I, F>> {

	/**
	 * Obtains the alias for the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return Alias for the {@link GovernanceSource} {@link Class}.
	 */
	String getGovernanceSourceAlias();

	/**
	 * Obtains the {@link GovernanceSource} {@link Class}.
	 * 
	 * @return {@link GovernanceSource} {@link Class}.
	 */
	Class<S> getGovernanceSourceClass();

}
