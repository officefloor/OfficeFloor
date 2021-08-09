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

package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Implementation of the {@link EscalationProcedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationProcedureImpl implements EscalationProcedure {

	/**
	 * {@link EscalationFlow} instances in order for this procedure.
	 */
	private final EscalationFlow[] escalations;

	/**
	 * Initiate with {@link EscalationFlow} details.
	 * 
	 * @param escalations
	 *            {@link EscalationFlow} instances in order to be taken for this
	 *            procedure.
	 */
	public EscalationProcedureImpl(EscalationFlow... escalations) {
		this.escalations = escalations;
	}

	/*
	 * ============= EscalationProcedure ==================================
	 */

	@Override
	public EscalationFlow getEscalation(Throwable cause) {

		// Find the first matching escalation
		for (EscalationFlow escalation : this.escalations) {
			if (escalation.getTypeOfCause().isInstance(cause)) {
				// Use first matching
				return escalation;
			}
		}

		// Not found
		return null;
	}

}
