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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * Factory for the creation of an {@link OfficeTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamRegistry {

	/**
	 * Obtains the {@link OfficeTeamNode} instances.
	 * 
	 * @return {@link OfficeTeamNode} instances.
	 */
	OfficeTeamNode[] getOfficeTeams();

	/**
	 * <p>
	 * Creates the {@link OfficeTeamNode}.
	 * <p>
	 * The name of the {@link OfficeTeamNode} may be adjusted to ensure
	 * uniqueness.
	 * 
	 * @param officeTeamName
	 *            {@link OfficeTeam} name.
	 * @return {@link OfficeTeamNode}.
	 */
	OfficeTeamNode createOfficeTeam(String officeTeamName);

}
