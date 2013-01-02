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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of the {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskTypeBuilder<M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Specifies the differentiator.
	 * 
	 * @param differentiator
	 *            Differentiator.
	 * 
	 * @see TaskBuilder#setDifferentiator(Object)
	 */
	void setDifferentiator(Object differentiator);

	/**
	 * Specifies the type of {@link Object} returned from the {@link Task} that
	 * is to be used as the argument to the next {@link Task}.
	 * 
	 * @param returnType
	 *            Return type of the {@link Task}.
	 */
	void setReturnType(Class<?> returnType);

	/**
	 * <p>
	 * Adds a {@link TaskObjectTypeBuilder} to the {@link TaskTypeBuilder}
	 * definition.
	 * <p>
	 * Should the dependent {@link Object} instances be {@link Indexed}, the
	 * order they are added is the order of indexing (starting at 0).
	 * 
	 * @param objectType
	 *            Type of the dependent {@link Object}.
	 * @return {@link TaskObjectTypeBuilder} to provide the
	 *         <code>type definition</code> of the added dependent
	 *         {@link Object}.
	 */
	TaskObjectTypeBuilder<M> addObject(Class<?> objectType);

	/**
	 * <p>
	 * Adds a {@link TaskFlowTypeBuilder} to the {@link TaskTypeBuilder}
	 * definition.
	 * <p>
	 * Should the {@link JobSequence} instigation be {@link Indexed}, the order they
	 * are added is the order of indexing (starting at 0).
	 * 
	 * @return {@link TaskFlowTypeBuilder} to provide the
	 *         <code>type definition</code> of the possible instigated
	 *         {@link JobSequence} by the {@link Task}.
	 */
	TaskFlowTypeBuilder<F> addFlow();

	/**
	 * Adds a {@link TaskEscalationTypeBuilder} to the {@link TaskTypeBuilder}
	 * definition.
	 * 
	 * @param escalationType
	 *            Type to be handled by an {@link EscalationFlow}.
	 * @return {@link TaskEscalationTypeBuilder} to provide the
	 *         <code>type definition</code>.
	 */
	<E extends Throwable> TaskEscalationTypeBuilder addEscalation(
			Class<E> escalationType);

}