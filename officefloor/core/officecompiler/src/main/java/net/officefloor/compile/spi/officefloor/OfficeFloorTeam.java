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
