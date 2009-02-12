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
package net.officefloor.frame.internal.structure;

import java.util.List;

/**
 * Container for an
 * {@link net.officefloor.frame.spi.administration.Administrator}.
 * 
 * @author Daniel
 */
public interface AdministratorContainer<I extends Object, A extends Enum<A>> {

	/**
	 * Obtains the {@link ExtensionInterfaceMetaData} to obtain the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} extension
	 * interfaces to provide to
	 * {@link #doDuty(TaskDutyAssociation, I[], AdministratorContext)}.
	 * 
	 * @param context
	 *            {@link AdministratorContext} to do the administration within.
	 * @return {@link ExtensionInterfaceMetaData} to obtain the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         extension interfaces to provide to
	 *         {@link #doDuty(TaskDutyAssociation, I[], AdministratorContext)}.
	 */
	ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData(
			AdministratorContext context);

	/**
	 * Executes the {@link net.officefloor.frame.spi.administration.Duty}.
	 * 
	 * @param taskDutyAssociation
	 *            {@link TaskDutyAssociation} of
	 *            {@link net.officefloor.frame.spi.administration.Duty} to
	 *            execute for the {@link net.officefloor.frame.api.execute.Task}.
	 * @param extensionInterfaces
	 *            Extension interfaces to be administered.
	 * @param context
	 *            {@link AdministratorContext} to do the administration within.
	 * @throws Throwable
	 *             If duty fails.
	 */
	void doDuty(TaskDutyAssociation<A> taskDutyAssociation,
			List<I> extensionInterfaces, AdministratorContext context)
			throws Throwable;

}
