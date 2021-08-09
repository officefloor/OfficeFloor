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

package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;

/**
 * {@link ManagedFunctionGovernanceConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionGovernanceConfigurationImpl implements
		ManagedFunctionGovernanceConfiguration {

	/**
	 * {@link Governance} name.
	 */
	private final String governanceName;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            {@link Governance} name.
	 */
	public ManagedFunctionGovernanceConfigurationImpl(String governanceName) {
		this.governanceName = governanceName;
	}

	/*
	 * ===================== TaskGovernanceConfiguration =================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

}
