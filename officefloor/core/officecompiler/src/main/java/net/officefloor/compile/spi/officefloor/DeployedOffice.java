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

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Deployed {@link Office} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeployedOffice extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link DeployedOffice}.
	 * 
	 * @return Name of this {@link DeployedOffice}.
	 */
	String getDeployedOfficeName();

	/**
	 * Obtains the {@link DeployedOfficeInput} for the {@link OfficeAvailableSectionInputType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection} providing the
	 *            {@link OfficeAvailableSectionInputType}.
	 * @param inputName
	 *            Name of the {@link OfficeAvailableSectionInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String sectionName,
			String inputName);

	/**
	 * Obtains the {@link OfficeTeam} for the {@link OfficeTeamType}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link OfficeTeamType}.
	 * @return {@link OfficeTeam}.
	 */
	OfficeTeam getDeployedOfficeTeam(String officeTeamName);

	/**
	 * Obtains the {@link OfficeObject} for the {@link OfficeManagedObjectType}.
	 * 
	 * @param officeManagedObjectName
	 *            Name of the {@link OfficeManagedObjectType}.
	 * @return {@link OfficeObject}.
	 */
	OfficeObject getDeployedOfficeObject(String officeManagedObjectName);

}
