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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link EscalationConfiguration}.
 * 
 * @author Daniel
 */
public class EscalationConfigurationImpl implements EscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * Flag indicating to reset the {@link ThreadState}.
	 */
	private final boolean isResetThreadState;

	/**
	 * {@link TaskNodeReference}.
	 */
	private final TaskNodeReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param isResetThreadState
	 *            Flag indicating to reset the {@link ThreadState}.
	 * @param taskNodeReference
	 *            {@link TaskNodeReference}.
	 */
	public EscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, TaskNodeReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.isResetThreadState = isResetThreadState;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * ================= EscalationConfiguration ========================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public boolean isResetThreadState() {
		return this.isResetThreadState;
	}

	@Override
	public TaskNodeReference getTaskNodeReference() {
		return this.taskNodeReference;
	}

}