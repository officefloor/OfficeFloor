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
