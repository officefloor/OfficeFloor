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
package net.officefloor.plugin.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObject} for the JPA {@link EntityManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaEntityManagerManagedObject implements ManagedObject {

	/**
	 * {@link EntityManager}.
	 */
	private final EntityManager entityManager;

	/**
	 * Initiate.
	 * 
	 * @param entityManager
	 *            {@link EntityManager}.
	 */
	public JpaEntityManagerManagedObject(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Obtains the {@link EntityTransaction}.
	 * 
	 * @return {@link EntityTransaction}.
	 */
	public EntityTransaction getEntityTransaction() {
		return this.entityManager.getTransaction();
	}

	/**
	 * Closes the {@link EntityManager}.
	 */
	public void closeEntityManager() {
		this.entityManager.close();
	}

	/*
	 * ====================== ManagedObject ======================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.entityManager;
	}

}