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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of the {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskTypeBuilder<W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * <p>
	 * Adds a {@link TaskObjectTypeBuilder} to the {@link TaskTypeBuilder} definition.
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
	 * Adds a {@link TaskFlowTypeBuilder} to the {@link TaskTypeBuilder} definition.
	 * <p>
	 * Should the {@link Flow} instigation be {@link Indexed}, the order they
	 * are added is the order of indexing (starting at 0).
	 * 
	 * @return {@link TaskFlowTypeBuilder} to provide the <code>type definition</code>
	 *         of the possible instigated {@link Flow} by the {@link Task}.
	 */
	TaskFlowTypeBuilder<F> addFlow();

	/**
	 * Adds a {@link TaskEscalationTypeBuilder} to the {@link TaskTypeBuilder} definition.
	 * 
	 * @param escalationType
	 *            Type of possible {@link Escalation}.
	 * @return {@link TaskEscalationTypeBuilder} to provide the
	 *         <code>type definition</code> of the possible {@link Escalation}
	 *         by the {@link Task}.
	 */
	<E extends Throwable> TaskEscalationTypeBuilder addEscalation(
			Class<E> escalationType);

}