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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.Executive;

/**
 * Factory for the {@link ManagedExecution}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedExecutionFactory {

	/**
	 * Creates the {@link ManagedExecution}.
	 * 
	 * @param           <E> Possible {@link Escalation} from {@link Execution}.
	 * @param executive {@link Executive}.
	 * @param execution {@link Execution}.
	 * @return {@link ManagedExecution}.
	 */
	<E extends Throwable> ManagedExecution<E> createManagedExecution(Executive executive, Execution<E> execution);

}
