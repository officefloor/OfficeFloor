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

package net.officefloor.frame.api.team;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Context for the {@link ThreadLocalAwareTeam} {@link Team}.
 *
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareContext {

	/**
	 * Executes the {@link Job} within the invoking {@link Thread} of the
	 * {@link ProcessState} to respect the {@link ThreadLocal} instances.
	 * 
	 * @param job
	 *            {@link Job} to be executed.
	 */
	void execute(Job job);

}
