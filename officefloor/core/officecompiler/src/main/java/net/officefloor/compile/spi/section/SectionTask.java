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
package net.officefloor.compile.spi.section;

import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;

/**
 * {@link Task} for a {@link SectionWork}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionTask {

	/**
	 * Obtains the name of this {@link SectionTask}.
	 * 
	 * @return Name of this {@link SectionTask}.
	 */
	String getSectionTaskName();

	/**
	 * Obtains the {@link TaskFlow} for the {@link TaskFlowType}.
	 * 
	 * @param taskFlowName
	 *            Name of the {@link TaskFlowType}.
	 * @return {@link TaskFlow}.
	 */
	TaskFlow getTaskFlow(String taskFlowName);

	/**
	 * Obtains the {@link TaskObject} for the {@link TaskObjectType}.
	 * 
	 * @param taskObjectName
	 *            Name of the {@link TaskObjectType}.
	 * @return {@link TaskObject}.
	 */
	TaskObject getTaskObject(String taskObjectName);

	/**
	 * Obtains the {@link TaskFlow} for the {@link TaskEscalationType}.
	 * 
	 * @param escalationType
	 *            Fully qualified class name of the {@link Throwable}
	 *            identifying the {@link TaskEscalationType}. The
	 *            {@link Escalation} type is used rather than the name as
	 *            handling is done by the {@link Escalation} type.
	 * @return {@link TaskFlow} for the {@link TaskEscalationType}.
	 */
	TaskFlow getTaskEscalation(String escalationType);

}