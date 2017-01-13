/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.duty;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;

/**
 * Implementation of {@link ManagedFunctionDutyAssociation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionDutyAssociationImpl<A extends Enum<A>> implements
		ManagedFunctionDutyAssociation<A> {

	/**
	 * {@link AdministratorIndex} identifying the {@link Administration}.
	 */
	private final AdministratorIndex adminIndex;

	/**
	 * {@link DutyKey} identifying the {@link AdministrationDuty} of the {@link Administration}
	 * .
	 */
	private final DutyKey<A> dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param adminIndex
	 *            {@link AdministratorIndex} identifying the
	 *            {@link Administration}.
	 * @param dutyKey
	 *            {@link DutyKey} identifying the {@link AdministrationDuty} of the
	 *            {@link Administration}.
	 */
	public ManagedFunctionDutyAssociationImpl(AdministratorIndex adminIndex,
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