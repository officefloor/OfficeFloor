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
 * Context for executing a {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadStateContext {

	/**
	 * Indicates if the {@link ThreadState} is safe on {@link Thread}.
	 * 
	 * @return <code>true</code> if {@link ThreadState} safe on {@link Thread}.
	 */
	boolean isThreadStateSafe();

	/**
	 * Indicates if the {@link ThreadState} requires {@link Thread} safety.
	 * 
	 * @return <code>true</code> if {@link ThreadState} requires {@link Thread}
	 *         safety.
	 */
	boolean isRequireThreadStateSafety();

	/**
	 * Flag the {@link ThreadState} requires {@link Thread} safety.
	 */
	void flagRequiresThreadStateSafety();

	/**
	 * Creates a {@link FunctionState} for the {@link FunctionLogic} under the
	 * current {@link ThreadState}.
	 * 
	 * @param logic               {@link FunctionLogic}.
	 * @param fallbackThreadState Fall back {@link ThreadState} should there be no
	 *                            current {@link ThreadState}.
	 * @return {@link FunctionState} for the {@link FunctionLogic}.
	 */
	FunctionState createFunction(FunctionLogic logic, ThreadState fallbackThreadState);

	/**
	 * Executes the {@link FunctionState} returning the next {@link FunctionState}
	 * to execute.
	 * 
	 * @param function {@link FunctionState} to be executed.
	 * @return Next {@link FunctionState} to be executed. May be <code>null</code>.
	 * @throws Throwable Possible failure in executing the {@link FunctionState}.
	 */
	FunctionState executeFunction(FunctionState function) throws Throwable;

	/**
	 * Obtains the {@link ManagedObjectContainer} for the
	 * {@link ManagedObjectIndex}.
	 * 
	 * @param index {@link ManagedObjectIndex}.
	 * @return {@link ManagedObjectContainer}. May be <code>null</code> if not yet
	 *         instantiated.
	 */
	ManagedObjectContainer getManagedObject(ManagedObjectIndex index);

}
