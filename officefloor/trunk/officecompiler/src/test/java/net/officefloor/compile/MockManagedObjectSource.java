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
package net.officefloor.compile;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Mock {@link ManagedObjectSource} for testing functionality.
 * 
 * @author Daniel
 */
public class MockManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadSpecification(net.officefloor.frame.spi
	 * .managedobject.source.impl.AbstractAsyncManagedObjectSource
	 * .SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadSpecification");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadMetaData(net.officefloor.frame.spi.managedobject
	 * .source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
			throws Exception {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadMetaData");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement AbstractManagedObjectSource<Indexed,Indexed>.getManagedObject");
	}

}
