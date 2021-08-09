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

package net.officefloor.frame.api.function;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;

/**
 * <p>
 * Callback for completion of a {@link Flow}.
 * <p>
 * The return state of the {@link Flow} is available from the
 * {@link ManagedObject} instances manipulated by the {@link Flow} (hence there
 * is no returned value to the {@link FlowCallback}).
 *
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FlowCallback {

	/**
	 * {@link FlowCallback} that simply escalates the {@link Flow}
	 * {@link Escalation} to the invoking {@link ManagedFunctionContainer} (i.e.
	 * {@link ManagedFunction} invoking the {@link Flow}).
	 */
	public static FlowCallback ESCALATE = (escalation) -> {
		if (escalation != null) {
			throw escalation;
		}
	};

	/**
	 * Invoked on completion of the {@link Flow}.
	 * 
	 * @param escalation Possible {@link Throwable} from the {@link Flow}.
	 *                   <code>null</code> indicates all {@link Escalation}
	 *                   instances handled within {@link Flow}.
	 * @throws Throwable {@link Escalation} within the callback logic.
	 */
	void run(Throwable escalation) throws Throwable;

}
