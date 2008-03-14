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
package net.officefloor.managedobjects;

import java.util.Properties;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Mock {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TestManagedObjectSource extends AbstractManagedObjectSource
		implements ManagedObject {

	/**
	 * Properties.
	 */
	public Properties properties;

	/**
	 * {@link Handler} for the {@link HandlerEnum#ADDED}.
	 */
	public TestHandler<Indexed> addedHandler;

	/*
	 * ====================================================================
	 * AbstractManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext context) throws Exception {

		// Obtain the source context
		ManagedObjectSourceContext sourceContext = context
				.getManagedObjectSourceContext();

		// Obtain the properties
		this.properties = sourceContext.getProperties();

		// Add handlers
		HandlerLoader<HandlerEnum> handlerLoader = context
				.getHandlerLoader(HandlerEnum.class);
		handlerLoader.mapHandlerType(HandlerEnum.ADDED, Handler.class);

		// Provide the added handler
		ManagedObjectHandlerBuilder<HandlerEnum> mosHandlerBuilder = sourceContext
				.getHandlerBuilder(HandlerEnum.class);
		HandlerBuilder<Indexed> addedHandlerBuilder = mosHandlerBuilder
				.registerHandler(HandlerEnum.ADDED);
		addedHandlerBuilder
				.setHandlerFactory(new TestHandler<Indexed>("ADDED"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void start(StartContext startContext) {

		// Obtain the execute context
		ManagedObjectExecuteContext<HandlerEnum> context = startContext
				.getContext(HandlerEnum.class);

		// Obtain the added handler
		this.addedHandler = (TestHandler<Indexed>) context
				.getHandler(HandlerEnum.ADDED);
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
	 * ====================================================================
	 * ManagedObject
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() throws Exception {
		return this;
	}

	public static enum HandlerEnum {
		ADDED
	}

	/**
	 * {@link HandlerFactory} that returns this as the {@link Handler}.
	 */
	public static class TestHandler<F extends Enum<F>> implements
			HandlerFactory<F>, Handler<F> {

		/**
		 * Identifier for this {@link Handler}.
		 */
		private final String handlerId;

		/**
		 * {@link HandlerContext}.
		 */
		protected HandlerContext<F> context;

		/**
		 * Initiate.
		 * 
		 * @param handlerId
		 *            Identifier for this {@link Handler}.
		 */
		public TestHandler(String handlerId) {
			this.handlerId = handlerId;
		}

		/**
		 * Obtains the identifier of this {@link Handler}.
		 * 
		 * @return Identifier of this {@link Handler}.
		 */
		public String getHandlerId() {
			return this.handlerId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
		 */
		@Override
		public Handler<F> createHandler() {
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
		 */
		@Override
		public void setHandlerContext(HandlerContext<F> context)
				throws Exception {
			this.context = context;
		}
	}

}
