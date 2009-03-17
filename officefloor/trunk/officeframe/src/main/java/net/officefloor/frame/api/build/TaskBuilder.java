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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Builder of the {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskBuilder<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		extends FlowNodeBuilder<F> {

	/**
	 * Links in a {@link ManagedObject} to this {@link Task}.
	 * 
	 * @param key
	 *            Key identifying the {@link ManagedObject}.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	void linkManagedObject(M key, String scopeManagedObjectName);

	/**
	 * Links in a {@link ManagedObject} to this {@link Task}.
	 * 
	 * @param managedObjectIndex
	 *            Index of the {@link ManagedObject}.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	void linkManagedObject(int managedObjectIndex, String scopeManagedObjectName);

	/**
	 * Links in a {@link Duty} to be executed before the {@link Task}.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 */
	<A extends Enum<A>> void linkPreTaskAdministration(
			String scopeAdministratorName, A dutyKey);

	/**
	 * Links in a {@link Duty} to be executed after the {@link Task}.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 */
	<A extends Enum<A>> void linkPostTaskAdministration(
			String scopeAdministratorName, A dutyKey);

}