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