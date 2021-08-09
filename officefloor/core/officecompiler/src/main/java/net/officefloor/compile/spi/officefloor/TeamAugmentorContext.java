/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link TeamAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamAugmentorContext extends SourceIssues {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link TeamType} of the {@link Team}.
	 * 
	 * @return {@link Team} of the {@link Team}.
	 */
	TeamType getTeamType();

	/**
	 * Requests no {@link TeamOversight} for the {@link Team}.
	 */
	void requestNoTeamOversight();

}
