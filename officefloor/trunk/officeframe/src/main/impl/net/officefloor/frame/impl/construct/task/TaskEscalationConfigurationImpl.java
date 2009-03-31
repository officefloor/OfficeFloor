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

import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * Implementation of the {@link TaskEscalationConfiguration}.
 * 
 * @author Daniel
 */
public class TaskEscalationConfigurationImpl implements TaskEscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link TaskNodeReference}.
	 */
	private final TaskNodeReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param taskNodeReference
	 *            {@link TaskNodeReference}.
	 */
	public TaskEscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			TaskNodeReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
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
	public TaskNodeReference getTaskNodeReference() {
		return this.taskNodeReference;
	}

}