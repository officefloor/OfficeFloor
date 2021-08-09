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

import net.officefloor.frame.api.team.Team;

/**
 * Encapsulates simple logic for a {@link FunctionState}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionLogic {

	/**
	 * Obtains the responsible {@link TeamManagement} for this
	 * {@link FunctionLogic}.
	 * 
	 * @return {@link TeamManagement} responsible for this
	 *         {@link FunctionLogic}. May be <code>null</code> to use any
	 *         {@link Team}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null;
	}

	/**
	 * Indicates if the {@link FunctionLogic} requires {@link ThreadState}
	 * safety.
	 * 
	 * @return <code>true</code> should {@link FunctionLogic} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the logic.
	 * 
	 * @param flow
	 *            {@link Flow} that contains this {@link FunctionLogic}.
	 * @return Optional {@link FunctionState} to execute next.
	 * @throws Throwable
	 *             If logic fails.
	 */
	FunctionState execute(Flow flow) throws Throwable;

}
