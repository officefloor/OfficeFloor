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

import net.officefloor.frame.api.team.Job;

/**
 * Executes {@link Job} instances to enable access to the invoking
 * {@link ProcessState} {@link Thread} {@link ThreadLocal} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareExecutor {

	/**
	 * <p>
	 * Runs the {@link ProcessState} within context to enable the
	 * {@link ThreadLocal} instances of the current {@link Thread} to be
	 * available.
	 * <p>
	 * This will block the current {@link Thread} until the {@link ProcessState}
	 * and all subsequent {@link ProcessState} instances invoked by the current
	 * {@link Thread} are complete.
	 * 
	 * @param function
	 *            Initial {@link FunctionState} of the {@link ProcessState}.
	 * @param loop
	 *            {@link FunctionLoop}.
	 */
	void runInContext(FunctionState function, FunctionLoop loop);

	/**
	 * Executes the {@link Job} by the {@link Thread} registered to its
	 * {@link ProcessState}.
	 * 
	 * @param job
	 *            {@link Job}.
	 */
	void execute(Job job);

	/**
	 * Flags the {@link ProcessState} as complete.
	 * 
	 * @param processState
	 *            {@link ProcessState}.
	 */
	void processComplete(ProcessState processState);

}
