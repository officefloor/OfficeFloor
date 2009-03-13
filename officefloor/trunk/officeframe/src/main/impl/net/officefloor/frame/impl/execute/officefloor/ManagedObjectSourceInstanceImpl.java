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
package net.officefloor.frame.impl.execute.officefloor;

import java.util.Map;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSourceInstance} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceInstanceImpl<H extends Enum<H>> implements
		ManagedObjectSourceInstance<H> {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, H> managedObjectSource;

	/**
	 * {@link Handler} instances by their key for the
	 * {@link ManagedObjectSource}.
	 */
	private final Map<H, Handler<?>> handlers;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param handlers
	 *            {@link Handler} instances by their key for the
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 */
	public ManagedObjectSourceInstanceImpl(
			ManagedObjectSource<?, H> managedObjectSource,
			Map<H, Handler<?>> handlers, ManagedObjectPool managedObjectPool) {
		this.managedObjectSource = managedObjectSource;
		this.handlers = handlers;
		this.managedObjectPool = managedObjectPool;
	}

	/*
	 * ==================== ManagedObjectSourceInstance ==================
	 */

	@Override
	public ManagedObjectSource<?, H> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public Map<H, Handler<?>> getHandlers() {
		return this.handlers;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

}
