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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * Configuration for a {@link Flow}.
 * 
 * @author Daniel
 */
public interface TaskFlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of this {@link Flow}.
	 * 
	 * @return Name of this {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the strategy to instigate this {@link Flow}.
	 * 
	 * @return Strategy to instigate this {@link Flow}.
	 */
	FlowInstigationStrategyEnum getInstigationStrategy();

	/**
	 * Obtains the reference to the initial {@link Task} of this {@link Flow}.
	 * 
	 * @return Reference to the initial {@link Task} of this {@link Flow}.
	 */
	TaskNodeReference getInitialTask();

	/**
	 * Obtains the index identifying this {@link Flow}.
	 * 
	 * @return Index identifying this {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying this {@link Flow}.
	 * 
	 * @return Key identifying this {@link Flow}. <code>null</code> if indexed.
	 */
	F getKey();

}