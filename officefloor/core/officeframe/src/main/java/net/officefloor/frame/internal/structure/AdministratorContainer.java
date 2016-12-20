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
package net.officefloor.frame.internal.structure;

import java.util.List;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Container for an {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorContainer<I extends Object, A extends Enum<A>> {

	/**
	 * Obtains the {@link ExtensionInterfaceMetaData} to obtain the
	 * {@link ManagedObject} extension interfaces to provide to the {@link Duty}
	 * .
	 * 
	 * @param context
	 *            {@link AdministratorContext} for the {@link Administrator} to
	 *            administer within.
	 * @return {@link ExtensionInterfaceMetaData} to obtain the
	 *         {@link ManagedObject} extension interfaces to provide to the
	 *         {@link Duty}.
	 */
	ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData(AdministratorContext context);

	/**
	 * Executes the {@link Duty}.
	 * 
	 * @param taskDutyAssociation
	 *            {@link TaskDutyAssociation} of {@link Duty} to execute for the
	 *            {@link Task}.
	 * @param extensionInterfaces
	 *            Extension interfaces to be administered.
	 * @param context
	 *            {@link AdministratorContext} for the {@link Duty} be executed
	 *            within.
	 * @return Optional {@link Job} to undertake the {@link Duty}.
	 * @throws Throwable
	 *             If {@link Duty} fails.
	 */
	FunctionState doDuty(TaskDutyAssociation<A> taskDutyAssociation, List<I> extensionInterfaces,
			AdministratorContext context) throws Throwable;

}