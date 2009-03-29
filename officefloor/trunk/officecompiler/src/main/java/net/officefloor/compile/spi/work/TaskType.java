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
package net.officefloor.compile.spi.work;

import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskType<W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Task}.
	 * 
	 * @return Name of the {@link Task}.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link TaskFactoryManufacturer}.
	 * 
	 * @return {@link TaskFactoryManufacturer}.
	 */
	TaskFactoryManufacturer<W, M, F> getTaskFactoryManufacturer();

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