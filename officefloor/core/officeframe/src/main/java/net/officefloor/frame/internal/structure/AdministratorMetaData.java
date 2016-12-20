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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorMetaData<I extends Object, A extends Enum<A>> extends ManagedFunctionMetaData {

	/**
	 * Creates a new {@link AdministratorContainer} from this
	 * {@link AdministratorMetaData}.
	 * 
	 * @return New {@link AdministratorContainer}.
	 */
	AdministratorContainer<I, A> createAdministratorContainer();

	/**
	 * Obtains the {@link AdministratorSource}.
	 * 
	 * @return {@link AdministratorSource}.
	 */
	AdministratorSource<I, A> getAdministratorSource();

	/**
	 * Obtains the {@link ExtensionInterfaceMetaData} over the
	 * {@link ManagedObject} instances to be administered by this
	 * {@link Administrator}.
	 * 
	 * @return {@link ExtensionInterfaceMetaData} over the {@link ManagedObject}
	 *         instances to be administered by this {@link Administrator}.
	 */
	ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData();

	/**
	 * Obtains the {@link DutyMetaData} for the input {@link DutyKey}.
	 * 
	 * @param dutyKey
	 *            {@link DutyKey} specifying the {@link Duty}.
	 * @return {@link DutyMetaData} for {@link Duty} identified by the
	 *         {@link DutyKey}.
	 */
	DutyMetaData getDutyMetaData(DutyKey<A> dutyKey);

	/**
	 * Creates the {@link ManagedFunction} for the {@link Duty}.
	 * 
	 * @param administeringTaskMetaData
	 *            {@link TaskMetaData} of the administered {@link Task}.
	 * @param administeringWorkContainer
	 *            {@link WorkContainer} of the administered {@link Task}.
	 * @param flow
	 *            {@link Flow}.
	 * @param taskDutyAssociation
	 *            {@link TaskDutyAssociation}.
	 * @param parallelJobNodeOwner
	 *            Paralllel {@link ManagedFunction} owner.
	 * @return {@link ManagedFunction} for the {@link Duty}.
	 */
	ManagedFunction createDutyNode(TaskMetaData<?, ?, ?> administeringTaskMetaData,
			WorkContainer<?> administeringWorkContainer, Flow flow, TaskDutyAssociation<?> taskDutyAssociation,
			ManagedFunction parallelJobNodeOwner);

}