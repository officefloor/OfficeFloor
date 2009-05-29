/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link OfficeEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancerContext {

	/**
	 * Obtains the {@link FlowNodeBuilder} registered under the input
	 * {@link Work} and {@link Task} names.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return {@link FlowNodeBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String workName, String taskName);

	/**
	 * Obtains the {@link FlowNodeBuilder} registered by the
	 * {@link ManagedObjectSource} under the input {@link Work} and {@link Task}
	 * names.
	 * 
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} name registered with the
	 *            {@link OfficeFloorBuilder}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return {@link FlowNodeBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String managedObjectSourceName,
			String workName, String taskName);

}