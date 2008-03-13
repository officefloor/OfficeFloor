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
package net.officefloor.frame.api.construct;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests {@link ManagedObject} that invokes a {@link Task} of the {@link Office}
 * but is not used by the {@link Office}.
 * 
 * @author Daniel
 */
public class ManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private static TestManagedObjectSource managedObjectSource;

	/**
	 * {@link TestHandler}.
	 */
	private static TestHandler handler;

	/**
	 * Ensures able to construct a {@link ManagedObject}.
	 */
	public void testManagedObject() throws Exception {

		final String MANAGING_OFFICE = "MANAGING_OFFICE";

		// Create and register the managed object source
		ManagedObjectBuilder managedObjectBuilder = OfficeFrame.getInstance()
				.getBuilderFactory().createManagedObjectBuilder();
		managedObjectBuilder
				.setManagedObjectSourceClass(TestManagedObjectSource.class);
		managedObjectBuilder.setManagingOffice(MANAGING_OFFICE);
		this.getOfficeFloorBuilder().addManagedObject("MO",
				managedObjectBuilder);

		// Construct and open the office floor
		OfficeFloor officeFloor = this.constructOfficeFloor(MANAGING_OFFICE);
		officeFloor.openOfficeFloor();

		// Ensure the managed object source created
		assertNotNull("Managed Object Source not created", managedObjectSource);

		// Ensure the handler created
		assertNotNull("Handler not created", handler);
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	public static class TestManagedObjectSource extends
			AbstractManagedObjectSource implements ManagedObject {

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {
			// Specify managed object source
			ManagedObjectTest.managedObjectSource = this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
		 */
		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No requirements
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
		 */
		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			// Load the handlers
			HandlerLoader<HandlerKey> handlerLoader = context
					.getHandlerLoader(HandlerKey.class);
			handlerLoader.mapHandlerType(HandlerKey.HANDLER, Handler.class);

			// Provide the handler
			ManagedObjectHandlerBuilder<HandlerKey> moHandlerBuilder = context
					.getManagedObjectSourceContext().getHandlerBuilder(
							HandlerKey.class);
			HandlerBuilder<Indexed> handlerBuilder = moHandlerBuilder
					.registerHandler(HandlerKey.HANDLER);
			handlerBuilder.setHandlerFactory(new TestHandler());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)
		 */
		@Override
		protected void start(StartContext startContext) throws Exception {
			// Specify the handler
			handler = (TestHandler) startContext.getContext(HandlerKey.class)
					.getHandler(HandlerKey.HANDLER);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
		 */
		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
		 */
		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * Test {@link Handler} keys.
	 */
	public enum HandlerKey {
		HANDLER
	}

	/**
	 * Test {@link Handler}.
	 */
	private static class TestHandler implements HandlerFactory<Indexed>,
			Handler<Indexed> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
		 */
		@Override
		public Handler<Indexed> createHandler() {
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
		 */
		@Override
		public void setHandlerContext(HandlerContext<Indexed> context)
				throws Exception {
			// Do nothing
		}

	}

}
