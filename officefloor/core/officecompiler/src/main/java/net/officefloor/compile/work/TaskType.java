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
package net.officefloor.compile.work;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskType<W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Task}.
	 * 
	 * @return Name of the {@link Task}.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link TaskFactory}.
	 * 
	 * @return {@link TaskFactory}.
	 */
	TaskFactory<W, M, F> getTaskFactory();

	/**
	 * Obtains the differentiator.
	 * 
	 * @return Differentiator.
	 * 
	 * @see TaskBuilder#setDifferentiator(Object)
	 */
	Object getDifferentiator();

	/**
	 * Obtains the type of {@link Object} returned from the {@link Task} that is
	 * to be used as the argument to the next {@link Task}.
	 * 
	 * @return Return type of the {@link Task}.
	 */
	Class<?> getReturnType();

	/**
	 * Obtains the {@link Enum} providing the keys for the dependent
	 * {@link Object} instances.
	 * 
	 * @return {@link Enum} providing the dependent {@link Object} keys or
	 *         <code>null</code> if {@link Indexed} or no dependencies.
	 */
	Class<M> getObjectKeyClass();

	/**
	 * Obtains the {@link TaskObjectType} definitions for the dependent
	 * {@link Object} instances required by the {@link Task}.
	 * 
	 * @return {@link TaskObjectType} definitions for the dependent
	 *         {@link Object} instances required by the {@link Task}.
	 */
	TaskObjectType<M>[] getObjectTypes();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link Task}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link TaskFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Task}.
	 * 
	 * @return {@link TaskFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link Task}.
	 */
	TaskFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link TaskEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link Task}.
	 * 
	 * @return {@link TaskEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Task}.
	 */
	TaskEscalationType[] getEscalationTypes();

}