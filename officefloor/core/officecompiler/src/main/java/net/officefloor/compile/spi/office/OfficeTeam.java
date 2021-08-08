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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorResponsibility;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} required by the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeam extends OfficeFloorResponsibility {

	/**
	 * Obtains the name of this {@link OfficeTeam}.
	 * 
	 * @return Name of this {@link OfficeTeam}.
	 */
	String getOfficeTeamName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link OfficeTeam}.
	 * <p>
	 * This enables distinguishing {@link OfficeTeam} instances to enable, for
	 * example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}
