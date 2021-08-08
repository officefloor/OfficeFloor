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

package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Oversight for a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamOversight {

	/**
	 * <p>
	 * Creates the {@link Team}.
	 * <p>
	 * This is expected to delegate to the {@link TeamSource} to create the
	 * {@link Team}. However, the {@link Executive} may decide to wrap the
	 * {@link Team} or provide multiple {@link Team} instances with assigning
	 * algorithm (such as taking advantage of {@link Thread} affinity). The choice
	 * is, however, ultimately left to the {@link Executive} to manage the
	 * {@link Team} instances.
	 *
	 * @param context {@link ExecutiveContext}.
	 * @return {@link Team}.
	 * @throws Exception If fails to configure the {@link TeamSource}.
	 */
	Team createTeam(ExecutiveContext context) throws Exception;

}
