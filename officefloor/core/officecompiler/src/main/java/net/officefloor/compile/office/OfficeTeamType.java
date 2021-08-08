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

package net.officefloor.compile.office;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamType {

	/**
	 * Obtains the name of the required {@link Team}.
	 * 
	 * @return Name of the required {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeTeam}.
	 */
	TypeQualification[] getTypeQualification();

}
