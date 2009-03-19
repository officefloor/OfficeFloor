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
package net.officefloor.compile.spi.pool;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * <code>Type definition</code> of a {@link ManagedObjectPool}.
 * <p>
 * All {@link ManagedObjectPool} instances implement the same interface. They
 * however differ in their characteristics which is internal to the
 * {@link ManagedObjectPool}.
 * <p>
 * TODO Need to consider if necessary to provide criteria on a
 * {@link ManagedObjectSource} to differentiate which {@link ManagedObjectPool}
 * may pool its {@link ManagedObject} instances.
 * 
 * @author Daniel
 */
public interface ManagedObjectPoolType {

}