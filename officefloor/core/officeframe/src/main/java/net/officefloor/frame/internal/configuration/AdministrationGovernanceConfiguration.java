/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * Configuration of linking {@link Governance} to {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceConfiguration<G extends Enum<G>> {

	/**
	 * Obtains the name of the {@link Governance} to link to the
	 * {@link Administration}.
	 * 
	 * @return Name of the {@link Governance} to link to the
	 *         {@link Administration}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the linked {@link Governance}.
	 * 
	 * @return Index identifying the linked {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the linked {@link Governance}.
	 * 
	 * @return Key identifying the linked {@link Governance}. <code>null</code>
	 *         if indexed.
	 */
	G getKey();

}
