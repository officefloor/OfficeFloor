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
package net.officefloor.frame.impl;

import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorImpl implements OfficeFloor {

	/**
	 * Set of {@link Team} instances.
	 */
	private final Set<Team> teams;

	/**
	 * Registry of {@link Office} instances.
	 */
	private final Map<String, OfficeImpl> offices;

	/**
	 * Initiate.
	 * 
	 * @param teams
	 *            Set of {@link Team} instances.
	 * @param offices
	 *            Registry of {@link Office} instances.
	 */
	public OfficeFloorImpl(Set<Team> teams, Map<String, OfficeImpl> offices) {
		this.teams = teams;
		this.offices = offices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeFloor#openOfficeFloor()
	 */
	public void openOfficeFloor() {
		// Start the teams working
		for (Team team : this.teams) {
			team.startWorking();
		}

		// Start the offices
		for (OfficeImpl office : this.offices.values()) {
			office.openOffice();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeFloor#closeOfficeFloor()
	 */
	public void closeOfficeFloor() {
		// Stop the teams of this Office working
		for (Team team : this.teams) {
			team.stopWorking();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeFloor#getOffice(java.lang.String)
	 */
	public Office getOffice(String officeName) {
		return this.offices.get(officeName);
	}

}
