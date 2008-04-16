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

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;

/**
 * Context for the {@link OfficeEnhancer}.
 * 
 * @author Daniel
 */
public interface OfficeEnhancerContext {

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

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectHandlerBuilder} for the input
	 * {@link ManagedObject} id.
	 * <p>
	 * It is anticipated that the {@link ManagedObjectSource} is being managed
	 * by the {@link Office} that this {@link OfficeEnhancer} was added.
	 * 
	 * @param managedObjectId
	 *            Id of the {@link ManagedObjectSource} registered with the
	 *            {@link OfficeFloorBuilder}.
	 * @param handlerKeys
	 *            {@link Enum} specifying the {@link Handler} instances. This
	 *            MUST match the {@link Enum} from the
	 *            {@link ManagedObjectSourceMetaData#getHandlerKeys()}.
	 * @return {@link ManagedObjectHandlerBuilder}.
	 * @throws BuildException
	 *             If fails to obtain the {@link ManagedObjectHandlerBuilder}.
	 */
	<H extends Enum<H>> ManagedObjectHandlerBuilder<H> getManagedObjectHandlerBuilder(
			String managedObjectId, Class<H> handlerKeys) throws BuildException;

}
