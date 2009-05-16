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
package net.officefloor.frame.impl.execute.duty;

import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyKey;

/**
 * Implementation of {@link TaskDutyAssociation}.
 * 
 * @author Daniel
 */
public class TaskDutyAssociationImpl<A extends Enum<A>> implements
		TaskDutyAssociation<A> {

	/**
	 * {@link AdministratorIndex} identifying the {@link Administrator}.
	 */
	private final AdministratorIndex adminIndex;

	/**
	 * {@link DutyKey} identifying the {@link Duty} of the {@link Administrator}
	 * .
	 */
	private final DutyKey<A> dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param adminIndex
	 *            {@link AdministratorIndex} identifying the
	 *            {@link Administrator}.
	 * @param dutyKey
	 *            {@link DutyKey} identifying the {@link Duty} of the
	 *            {@link Administrator}.
	 */
	public TaskDutyAssociationImpl(AdministratorIndex adminIndex,
			DutyKey<A> dutyKey) {
		this.adminIndex = adminIndex;
		this.dutyKey = dutyKey;
	}

	/*
	 * ================= TaskDutyAssociation ==================================
	 */

	@Override
	public AdministratorIndex getAdministratorIndex() {
		return this.adminIndex;
	}

	@Override
	public DutyKey<A> getDutyKey() {
		return this.dutyKey;
	}

}