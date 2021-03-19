/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
