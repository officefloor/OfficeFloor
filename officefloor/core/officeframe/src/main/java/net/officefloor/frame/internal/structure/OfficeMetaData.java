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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeMetaData {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains the {@link OfficeManager} of the {@link Office}.
	 * 
	 * @return {@link OfficeManager} of the {@link Office}.
	 */
	OfficeManager getOfficeManager();

	/**
	 * Obtains the {@link OfficeClock} for the {@link Office}.
	 * 
	 * @return {@link OfficeClock} for the {@link Office}.
	 */
	OfficeClock getOfficeClock();

	/**
	 * Obtains the {@link FunctionLoop} for the {@link Office}.
	 * 
	 * @return {@link FunctionLoop} for the {@link Office}.
	 */
	FunctionLoop getJobNodeLoop();

	/**
	 * Obtains the {@link ProcessMetaData} for processes within this
	 * {@link Office}.
	 * 
	 * @return {@link ProcessMetaData} for processes within this {@link Office}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * Obtains the {@link WorkMetaData} of the {@link Work} that may be done
	 * within this {@link Office}.
	 * 
	 * @return {@link WorkMetaData} instances of this {@link Office}.
	 */
	WorkMetaData<?>[] getWorkMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for this {@link Office}. This is
	 * used when the {@link EscalationProcedure} instances on the {@link Flow}
	 * does not handle the escalation.
	 * 
	 * @return {@link EscalationProcedure} for this {@link Office}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link OfficeStartupTask} instances for this {@link Office}.
	 * 
	 * @return {@link OfficeStartupTask} instances for this {@link Office}.
	 */
	OfficeStartupTask[] getStartupTasks();

	/**
	 * Creates a new {@link ProcessState}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link FunctionState} for the
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link FunctionState}.
	 * @param invocationEscalationHandler
	 *            Potential {@link EscalationHandler} provided by the invoker.
	 *            May be <code>null</code> to just use the default
	 *            {@link Office} {@link EscalationProcedure}.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link Escalation} handling.
	 * @return {@link FunctionState} to start processing the {@link ProcessState}.
	 */
	<W extends Work> FunctionState createProcess(FlowMetaData<W> flowMetaData, Object parameter,
			EscalationHandler invocationEscalationHandler, TeamManagement escalationResponsibleTeam);

	/**
	 * Creates a new {@link ProcessState} triggered by a
	 * {@link ManagedObjectSource} within the {@link Office} returning the
	 * starting {@link FunctionState} to be executed.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link FunctionState} for the
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link FunctionState}.
	 * @param invocationEscalationHandler
	 *            Potential {@link EscalationHandler} provided by the invoker.
	 *            May be <code>null</code> to just use the default
	 *            {@link Office} {@link EscalationProcedure}.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link Escalation} handling.
	 * @param inputManagedObject
	 *            {@link ManagedObject} that possibly invoked the new
	 *            {@link ProcessState}. This may be <code>null</code> and if so
	 *            the remaining parameters will be ignored.
	 * @param inputManagedObjectMetaData
	 *            {@link ManagedObjectMetaData} for the {@link ManagedObject}
	 *            that invoked the new {@link ProcessState}. Should the
	 *            {@link ManagedObject} be provided this must then also be
	 *            provided.
	 * @param processBoundIndexForInputManagedObject
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState}. Ignored if {@link ManagedObject} passed
	 *            in is <code>null</code>.
	 * @return {@link FunctionState} to start processing the {@link ProcessState}.
	 */
	<W extends Work> FunctionState createProcess(FlowMetaData<W> flowMetaData, Object parameter,
			EscalationHandler invocationEscalationHandler, TeamManagement escalationResponsibleTeam,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int processBoundIndexForInputManagedObject);

}