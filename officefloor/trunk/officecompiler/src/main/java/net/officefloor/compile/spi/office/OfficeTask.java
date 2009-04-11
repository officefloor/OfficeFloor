/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link OfficeTask} within the {@link Office}.
 * 
 * @author Daniel
 */
public interface OfficeTask {

	/**
	 * <p>
	 * Obtains the name of the {@link OfficeTask}.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeTask}.
	 * 
	 * @return Name of the {@link OfficeTask}.
	 */
	String getOfficeTaskName();

	/**
	 * <p>
	 * Obtains the {@link OfficeManagedObject} instances that this
	 * {@link OfficeTask} is dependent upon.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeTask}.
	 * 
	 * @return {@link OfficeManagedObject} instances that this
	 *         {@link OfficeTask} is dependent upon.
	 */
	OfficeManagedObject[] getDependentOfficeManagedObjects();

	/**
	 * Specifies the {@link Office} name of the {@link Team} responsible for
	 * this {@link OfficeTask}.
	 * 
	 * @param officeTeamName
	 *            {@link Office} name of the {@link Team} responsible for this
	 *            {@link OfficeTask}.
	 */
	void setTeamResponsible(String officeTeamName);

	/**
	 * <p>
	 * Adds an {@link OfficeDuty} to be done before attempting this
	 * {@link OfficeTask}.
	 * <p>
	 * The order that the {@link OfficeDuty} instances are added is the order
	 * they will be done before this {@link OfficeTask}.
	 * 
	 * @param duty
	 *            {@link OfficeDuty} to be done before attempting this
	 *            {@link OfficeTask}.
	 */
	void addPreTaskDuty(OfficeDuty duty);

	/**
	 * <p>
	 * Adds an {@link OfficeDuty} to be done after completing this
	 * {@link OfficeTask}.
	 * <p>
	 * The order that the {@link OfficeDuty} instances are added is the order
	 * they will be done after this {@link OfficeTask} is complete.
	 * 
	 * @param duty
	 *            {@link OfficeDuty} to be done after completing this
	 *            {@link OfficeTask}.
	 */
	void addPostTaskDuty(OfficeDuty duty);

}