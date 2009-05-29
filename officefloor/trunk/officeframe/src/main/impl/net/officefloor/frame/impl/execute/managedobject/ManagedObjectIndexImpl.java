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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObjectIndex} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectIndexImpl implements ManagedObjectIndex {

	/**
	 * {@link ManagedObjectScope}.
	 */
	private final ManagedObjectScope managedObjectScope;

	/**
	 * Index of the {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	private final int indexOfManagedObjectWithinScope;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param indexOfManagedObjectWithinScope
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public ManagedObjectIndexImpl(ManagedObjectScope managedObjectScope,
			int indexOfManagedObjectWithinScope) {
		this.managedObjectScope = managedObjectScope;
		this.indexOfManagedObjectWithinScope = indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= ManagedObjectIndex ===========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectIndex#
	 * getManagedObjectScope()
	 */
	@Override
	public ManagedObjectScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectIndex#
	 * getIndexOfManagedObjectWithinScope()
	 */
	@Override
	public int getIndexOfManagedObjectWithinScope() {
		return this.indexOfManagedObjectWithinScope;
	}

}
