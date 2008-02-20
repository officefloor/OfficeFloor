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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link FlowNodesEnhancer}.
 * 
 * @author Daniel
 */
public interface FlowNodesEnhancerContext {

	/**
	 * Obtains the {@link FlowNodeBuilder} registered under the input
	 * {@link Work} and {@link Task} names.
	 * 
	 * @param workName
	 *            Name of {@link Work}.
	 * @param taskName
	 *            Name of {@link Task}.
	 * @return {@link FlowNodeBuilder}.
	 * @throws BuildException
	 *             If fails to obtain the {@link FlowNodeBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String workName, String taskName)
			throws BuildException;

	/**
	 * Obtains the {@link FlowNodeBuilder} registered under the input
	 * {@link Work} and {@link Task} names.
	 * 
	 * @param namespace
	 *            Namespace to find the {@link FlowNodeBuilder}. This is
	 *            generally the {@link ManagedObjectSource} name registered with
	 *            the {@link OfficeFloorBuilder}.
	 * @param workName
	 *            Name of {@link Work}.
	 * @param taskName
	 *            Name of {@link Task}.
	 * @return {@link FlowNodeBuilder}.
	 * @throws BuildException
	 *             If fails to obtain the {@link FlowNodeBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String namespace, String workName,
			String taskName) throws BuildException;

}
