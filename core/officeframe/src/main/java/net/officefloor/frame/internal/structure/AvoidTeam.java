/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
 * Provides details to avoid execution with a particular {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AvoidTeam {

	/**
	 * Obtains the {@link FunctionState} to continue execution avoiding the
	 * specified {@link Team}.
	 * 
	 * @return {@link FunctionState} to continue execution avoiding the specified
	 *         {@link Team}.
	 */
	FunctionState getFunctionState();

	/**
	 * Flags to stop avoiding the {@link Team}.
	 */
	void stopAvoidingTeam();

}
