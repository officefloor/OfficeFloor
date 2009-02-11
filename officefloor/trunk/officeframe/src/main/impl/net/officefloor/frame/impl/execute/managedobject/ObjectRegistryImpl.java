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
package net.officefloor.frame.impl.execute.managedobject;

import java.util.Map;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;

/**
 * Implementation of the
 * {@link net.officefloor.frame.spi.managedobject.ObjectRegistry}.
 * 
 * @author Daniel
 */
public class ObjectRegistryImpl<D extends Enum<D>> implements ObjectRegistry<D> {

	/**
	 * {@link WorkContainer} to obtain the co-ordinating
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final WorkContainer<?> workContainer;

	/**
	 * Map of dependency key to the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} index.
	 */
	private final Map<D, Integer> dependencies;

	/**
	 * {@link ThreadState} requesting the co-ordinating of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final ThreadState threadState;

	/**
	 * Initiate.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer} to obtain the co-ordinating
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @param dependencies
	 *            Map of dependency key to
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            index.
	 * @param threadState
	 *            {@link ThreadState} requesting the co-ordinating of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 *            This is used to access the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            bound
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances.
	 */
	public ObjectRegistryImpl(WorkContainer<?> workContainer,
			Map<D, Integer> dependencies, ThreadState threadState) {
		this.workContainer = workContainer;
		this.dependencies = dependencies;
		this.threadState = threadState;
	}

	/*
	 * ====================================================================
	 * ManagedObjectMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ObjectRegistry#getObject(D)
	 */
	public Object getObject(D key) {
		// Obtain the managed object index
		Integer moIndex = this.dependencies.get(key);

		// Obtain the Object
		Object object = this.workContainer.getObject(moIndex.intValue(),
				this.threadState);

		// Return the Object
		return object;
	}

}
