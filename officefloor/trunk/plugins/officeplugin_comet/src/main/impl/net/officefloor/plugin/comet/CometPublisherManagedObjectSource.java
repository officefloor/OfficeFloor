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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.comet.spi.CometService;

/**
 * {@link ManagedObjectSource} for the {@link CometPublisher}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometPublisherManagedObjectSource
		extends
		AbstractManagedObjectSource<CometPublisherManagedObjectSource.Dependencies, None> {

	/**
	 * {@link CometPublisherManagedObjectSource} dependency keys.
	 */
	public static enum Dependencies {
		COMET_SERVICE
	}

	/**
	 * {@link ClassLoader} to create the {@link Proxy}.
	 */
	private ClassLoader classLoader;

	/*
	 * ===================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class loader
		this.classLoader = mosContext.getClassLoader();

		// Provide meta-data
		context.setObjectClass(CometPublisher.class);
		context.addDependency(Dependencies.COMET_SERVICE, CometService.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new CometPublisherManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link CometPublisher}.
	 */
	private class CometPublisherManagedObject implements
			CoordinatingManagedObject<Dependencies>, CometPublisher {

		/**
		 * {@link CometService}.
		 */
		private CometService service;

		/*
		 * =================== ManagedObject ======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {

			// Obtain the Comet Service
			this.service = (CometService) registry
					.getObject(Dependencies.COMET_SERVICE);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ================== CometPublisher =======================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <L extends CometSubscriber> L createPublisher(
				Class<L> listenerType, Object matchKey) {

			// Create the handler
			InvocationHandler handler = new CometPublisherInvocationHandler(
					listenerType, matchKey, this.service);

			// Return the proxy for publishing events
			return (L) Proxy.newProxyInstance(
					CometPublisherManagedObjectSource.this.classLoader,
					new Class<?>[] { listenerType }, handler);
		}
	}

	/**
	 * {@link InvocationHandler} for the {@link Proxy} created from the
	 * {@link CometPublisher}.
	 */
	private static class CometPublisherInvocationHandler implements
			InvocationHandler {

		/**
		 * Listener type.
		 */
		private final Class<?> listenerType;

		/**
		 * Match key.
		 */
		private final Object matchKey;

		/**
		 * {@link CometService}.
		 */
		private final CometService service;

		/**
		 * Initiate.
		 * 
		 * @param listenerType
		 *            Listener type.
		 * @param matchKey
		 *            Match key.
		 * @param service
		 *            {@link CometService}.
		 */
		public CometPublisherInvocationHandler(Class<?> listenerType,
				Object matchKey, CometService service) {
			this.listenerType = listenerType;
			this.matchKey = matchKey;
			this.service = service;
		}

		/*
		 * =================== InvocationHandler ==================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			// First parameter is the event data
			Object data = (args.length > 0 ? args[0] : null);

			// Publish the event
			this.service.publishEvent(this.listenerType, data, this.matchKey);

			// Nothing to return as should be void method
			return null;
		}
	}

}