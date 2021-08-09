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

package net.officefloor.compile.test.administration;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder of the {@link AdministrationType} to validate the loaded
 * {@link AdministrationType} from the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationTypeBuilder<F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Adds a {@link AdministrationFlowType}.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

	/**
	 * Adds an {@link AdministrationEscalationType}.
	 * 
	 * @param escalationName
	 *            Name of {@link AdministrationEscalationType}.
	 * @param escalationType
	 *            Type of {@link Escalation}.
	 */
	void addEscalation(String escalationName, Class<? extends Throwable> escalationType);

	/**
	 * Adds an {@link AdministrationGovernanceType}.
	 * 
	 * @param governanceName
	 *            Name of {@link Governance}.
	 * @param index
	 *            Index of the {@link Governance}.
	 * @param governanceKey
	 *            Key of the {@link Governance}.
	 */
	void addGovernance(String governanceName, int index, G governanceKey);

}
