/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

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
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolType {

}