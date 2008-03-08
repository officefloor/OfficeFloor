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
package net.officefloor.managedobjectsource;

import java.sql.Connection;

import junit.framework.Assert;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide testing of
 * {@link ManagedObjectSourceLoader}.
 * 
 * @author Daniel
 */
public class TestMetaDataManagedObjectSource extends
		AbstractManagedObjectSource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext context) throws Exception {

		// Load types
		context.setObjectClass(Object.class);
		context.setManagedObjectClass(ManagedObject.class);

		// Load dependencies
		DependencyLoader<DependencyKey> dependencyLoader = context
				.getDependencyLoader(DependencyKey.class);
		dependencyLoader.mapDependencyType(DependencyKey.DEPENDENCY,
				Connection.class);

		// Load handlers
		HandlerLoader<HandlerKey> handlerLoader = context
				.getHandlerLoader(HandlerKey.class);
		handlerLoader.mapHandlerType(HandlerKey.HANDLER, Handler.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		Assert.fail("getManagedObject should not be invoked");
		return null;
	}

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKey {
		DEPENDENCY
	}

	/**
	 * Handler keys.
	 */
	public static enum HandlerKey {
		HANDLER
	}
}
