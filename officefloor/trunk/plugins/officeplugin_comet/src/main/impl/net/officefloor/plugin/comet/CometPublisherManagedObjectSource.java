/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.comet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.comet.spi.CometServiceManagedObject.Dependencies;

/**
 * {@link ManagedObjectSource} for the {@link CometPublisher}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometPublisherManagedObjectSource extends
		AbstractManagedObjectSource<Dependencies, None> {

	/*
	 * ===================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractAsyncManagedObjectSource<DependencyKeys,None>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<DependencyKeys,None>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		// TODO implement
		// AbstractAsyncManagedObjectSource<DependencyKeys,None>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<DependencyKeys,None>.loadMetaData");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO implement
		// AbstractManagedObjectSource<DependencyKeys,None>.getManagedObject
		throw new UnsupportedOperationException(
				"TODO implement AbstractManagedObjectSource<DependencyKeys,None>.getManagedObject");
	}

}