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
package net.officefloor.frame.internal.structure;

import java.util.Map;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Instance of a {@link ManagedObjectSource} and items to support it.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceInstance<H extends Enum<H>> {

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, H> getManagedObjectSource();

	/**
	 * Obtains the map of {@link Handler} instances for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Map of {@link Handler} instances for the
	 *         {@link ManagedObjectSource}.
	 */
	Map<H, Handler<?>> getHandlers();

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if
	 *         {@link ManagedObjectSource} is not pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

}
