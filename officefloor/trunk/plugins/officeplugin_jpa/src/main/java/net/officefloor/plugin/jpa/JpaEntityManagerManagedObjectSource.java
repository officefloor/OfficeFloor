/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide a JPA {@link EntityManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaEntityManagerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Name of property providing the persistence unit name.
	 */
	public static final String PROPERTY_PERSISTENCE_UNIT_NAME = "persistence.unit.name";

	/*
	 * ================ ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractAsyncManagedObjectSource<None,None>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<None,None>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		// TODO implement
		// AbstractAsyncManagedObjectSource<None,None>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<None,None>.loadMetaData");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO implement
		// AbstractManagedObjectSource<None,None>.getManagedObject
		throw new UnsupportedOperationException(
				"TODO implement AbstractManagedObjectSource<None,None>.getManagedObject");
	}

}