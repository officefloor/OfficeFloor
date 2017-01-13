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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

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

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	@Override
	public int getIndexOfManagedObjectWithinScope() {
		return this.indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public String toString() {
		// Provide details of index
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append("[");
		text.append(this.managedObjectScope.toString());
		text.append(":");
		text.append(this.indexOfManagedObjectWithinScope);
		text.append("]");
		return text.toString();
	}

}