/*-
 * #%L
 * OfficeCompiler
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
