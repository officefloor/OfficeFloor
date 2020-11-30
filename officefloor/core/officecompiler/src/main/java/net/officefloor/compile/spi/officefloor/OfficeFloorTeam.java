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

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeam extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorTeam}.
	 * 
	 * @return Name of this {@link OfficeFloorTeam}.
	 */
	String getOfficeFloorTeamName();

	/**
	 * Specifies the size of the {@link Team}.
	 * 
	 * @param teamSize Size of the {@link Team}.
	 */
	void setTeamSize(int teamSize);

	/**
	 * Requests for no {@link TeamOversight} on this {@link Team}.
	 */
	void requestNoTeamOversight();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link OfficeFloorTeam}.
	 * <p>
	 * This enables distinguishing {@link OfficeFloorTeam} instances to enable, for
	 * example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualification.
	 * @param type      Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}
