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

import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * Configuration for a {@link net.officefloor.frame.internal.structure.Flow}.
 * 
 * @author Daniel
 */
public interface FlowConfiguration {

	/**
	 * Obtains the strategy to instigate this
	 * {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @return Strategy to instigate this
	 *         {@link net.officefloor.frame.internal.structure.Flow}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	FlowInstigationStrategyEnum getInstigationStrategy()
			throws ConfigurationException;

	/**
	 * Obtains the reference to the initial
	 * {@link net.officefloor.frame.api.execute.Task} of this
	 * {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @return Reference to the initial
	 *         {@link net.officefloor.frame.api.execute.Task} of this
	 *         {@link net.officefloor.frame.internal.structure.Flow}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	TaskNodeReference getInitialTask() throws ConfigurationException;

}
