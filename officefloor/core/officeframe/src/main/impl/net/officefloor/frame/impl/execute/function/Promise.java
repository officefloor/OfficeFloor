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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.FunctionState;

/**
 * Provides promise like functionality for {@link FunctionState} instances.
 *
 * @author Daniel Sagenschneider
 */
public class Promise {

	/**
	 * <p>
	 * Execute the {@link FunctionState} then the {@link FunctionState}.
	 * <p>
	 * State is passed between {@link FunctionState} instances via
	 * {@link ManagedObject} instances, so no parameter is provided.
	 * 
	 * @param function
	 *            {@link FunctionState} to execute it and its sequence of
	 *            {@link FunctionState} instances. May be <code>null</code>.
	 * @param thenFunction
	 *            {@link FunctionState} to then continue after the first input
	 *            {@link FunctionState} sequence completes. May be
	 *            <code>null</code>.
	 * @return Next {@link FunctionState} to undertake the {@link FunctionState}
	 *         sequence and then continue {@link FunctionState} sequence. Will
	 *         return <code>null</code> if both inputs are <code>null</code>.
	 */
	public static FunctionState then(FunctionState function, FunctionState thenFunction) {
		if (function == null) {
			// No initial function, so just continue
			return thenFunction;

		} else if (thenFunction != null) {

			// Create continue link
			return function.getThreadState().then(function, thenFunction);
		}

		// Only the initial function
		return function;
	}

	/**
	 * All access via static methods.
	 */
	private Promise() {
	}

}
