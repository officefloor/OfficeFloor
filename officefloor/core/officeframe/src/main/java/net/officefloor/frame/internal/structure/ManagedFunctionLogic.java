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

/**
 * Managed {@link FunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogic {

	/**
	 * Indicates if {@link ThreadState} safety is required for this
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return <code>true</code> should {@link ThreadState} safety be required for
	 *         this {@link ManagedFunctionLogic}.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the {@link ManagedFunctionLogic}.
	 * 
	 * @param context     {@link ManagedFunctionLogicContext}.
	 * @param threadState {@link ThreadState} for the {@link ManagedFunctionLogic}.
	 * @throws Throwable Failure of logic.
	 */
	void execute(ManagedFunctionLogicContext context, ThreadState threadState) throws Throwable;

}
