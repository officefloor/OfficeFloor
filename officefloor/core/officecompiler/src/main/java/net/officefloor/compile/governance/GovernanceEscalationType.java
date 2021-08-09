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

package net.officefloor.compile.governance;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link GovernanceActivity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceEscalationType {

	/**
	 * Obtains the name for the {@link GovernanceEscalationType}.
	 * 
	 * @return Name for the {@link GovernanceEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the
	 * {@link GovernanceActivity}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return Type of {@link EscalationFlow} by the {@link GovernanceActivity}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

}
