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

import net.officefloor.frame.internal.structure.TaskDutyAssociation;

/**
 * Implementation of
 * {@link net.officefloor.frame.internal.structure.TaskDutyAssociation}.
 * 
 * @author Daniel
 */
public class TaskDutyAssociationImpl<A extends Enum<A>> implements
		TaskDutyAssociation<A> {

	/**
	 * Index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	private final int adminIndex;

	/**
	 * Key identifying the {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	private final A dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param adminIndex
	 *            Index of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	public TaskDutyAssociationImpl(int adminIndex, A dutyKey) {
		this.adminIndex = adminIndex;
		this.dutyKey = dutyKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskDutyAssociation#getAdministratorIndex()
	 */
	public int getAdministratorIndex() {
		return this.adminIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskDutyAssociation#getDutyKey()
	 */
	public A getDutyKey() {
		return this.dutyKey;
	}

}
