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
import net.officefloor.frame.api.execute.ManagedFunction;
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
	FunctionLoop getFunctionLoop();

	/**
	 * Obtains the {@link ProcessMetaData} for processes within this
	 * {@link Office}.
	 * 
	 * @return {@link ProcessMetaData} for processes within this {@link Office}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the
	 * {@link ManagedFunction} that may be done within this {@link Office}.
	 * 
	 * @return {@link ManagedFunctionMetaData} instances of this {@link Office}.
	 */
	ManagedFunctionMetaData<?, ?>[] getManagedFunctionMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for this {@link Office}. This is
	 * used when the {@link EscalationProcedure} instances on the {@link Flow}
	 * does not handle the escalation.
	 * 
	 * @return {@link EscalationProcedure} for this {@link Office}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link OfficeStartupFunction} instances for this {@link Office}.
	 * 
	 * @return {@link OfficeStartupFunction} instances for this {@link Office}.
	 */
	OfficeStartupFunction[] getStartupTasks();

	/**
	 * Creates a new {@link ProcessState}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link FunctionState} for
	 *            the {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link FunctionState}.
	 * @param completion
	 *            Optional {@link FlowCompletion} of the {@link ProcessState}.
	 *            May be <code>null</code>.
	 * @return {@link ManagedFunctionContainer} to start processing the
	 *         {@link ProcessState}.
	 */
	ManagedFunctionContainer createProcess(FlowMetaData flowMetaData, Object parameter, FlowCompletion completion);

	/**
	 * Creates a new {@link ProcessState} triggered by a
	 * {@link ManagedObjectSource} within the {@link Office} returning the
	 * starting {@link FunctionState} to be executed.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link FunctionState} for
	 *            the {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link FunctionState}.
	 * @param completion
	 *            Optional {@link FlowCompletion} of the {@link ProcessState}.
	 *            May be <code>null</code>.
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
	 * @return {@link ManagedFunctionContainer} to start processing the
	 *         {@link ProcessState}.
	 */
	ManagedFunctionContainer createProcess(FlowMetaData flowMetaData, Object parameter, FlowCompletion completion,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int processBoundIndexForInputManagedObject);

}