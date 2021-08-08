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

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSectionFunction} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionFunction {

	/**
	 * Obtains the name of the {@link OfficeSectionFunction}.
	 * 
	 * @return Name of the {@link OfficeSectionFunction}.
	 */
	String getOfficeFunctionName();

	/**
	 * Obtains the {@link ResponsibleTeam} responsible for this
	 * {@link OfficeSectionFunction}.
	 * 
	 * @return {@link ResponsibleTeam} responsible for this
	 *         {@link OfficeSectionFunction}.
	 */
	ResponsibleTeam getResponsibleTeam();

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting this
	 * {@link OfficeSectionFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done before this {@link OfficeSectionFunction}.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting this
	 *            {@link OfficeSectionFunction}.
	 */
	void addPreAdministration(OfficeAdministration administration);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done after completing this
	 * {@link OfficeSectionFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done after this {@link OfficeSectionFunction} is complete.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done after completing this
	 *            {@link OfficeSectionFunction}.
	 */
	void addPostAdministration(OfficeAdministration administration);

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link OfficeSectionFunction}.
	 * <p>
	 * This enables specifying specifically which {@link OfficeSectionFunction}
	 * instances require {@link Governance}.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	void addGovernance(OfficeGovernance governance);

}
