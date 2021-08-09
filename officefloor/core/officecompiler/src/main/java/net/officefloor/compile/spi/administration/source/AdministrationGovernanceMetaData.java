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

package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.AdministrationMetaData;

/**
 * Describes a {@link Governance} used by the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceMetaData<G extends Enum<G>> {

	/**
	 * Obtains the {@link Enum} key identifying this {@link Governance}. If
	 * <code>null</code> then {@link Governance} will be referenced by this
	 * instance's index in the array returned from
	 * {@link AdministrationMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Governance} or
	 *         <code>null</code> indicating identified by an index.
	 */
	G getKey();

	/**
	 * Provides a descriptive name for this {@link Escalation}. This is useful
	 * to better describe the {@link Escalation}.
	 * 
	 * @return Descriptive name for this {@link Escalation}.
	 */
	String getLabel();

}
