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

package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * <code>Type definition</code> of a {@link Governance} used by
 * {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the {@link Governance}.
	 * 
	 * @return Index identifying the {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the {@link Governance}.
	 * 
	 * @return Key identifying the {@link Governance}.
	 */
	F getKey();

}
