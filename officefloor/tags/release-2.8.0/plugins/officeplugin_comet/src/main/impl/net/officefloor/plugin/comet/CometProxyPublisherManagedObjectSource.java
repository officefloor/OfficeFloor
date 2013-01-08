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
package net.officefloor.plugin.comet;

import java.lang.reflect.Proxy;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.comet.api.CometSubscriber;

/**
 * Provides the {@link Proxy} from the {@link CometPublisher} to enable typed
 * dependency for publishing.
 * 
 * @author Daniel Sagenschneider
 */
public class CometProxyPublisherManagedObjectSource
		extends
		AbstractManagedObjectSource<CometProxyPublisherManagedObjectSource.Dependencies, None> {

	/**
	 * {@link CometProxyPublisherManagedObjectSource} dependencies.
	 */
	public static enum Dependencies {
		COMET_PUBLISHER
	}

	/**
	 * Property name for the {@link Proxy} interface name for the
	 * {@link CometPublisher}.
	 */
	public static final String PROPERTY_PROXY_INTERFACE = "proxy.interface";

	/**
	 * {@link Proxy} interface for the {@link CometPublisher}.
	 */
	private Class<? extends CometSubscriber> proxyInterface;

	/*
	 * ==================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PROXY_INTERFACE, "Interface");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the proxy interface
		String proxyInterfaceName = mosContext
				.getProperty(PROPERTY_PROXY_INTERFACE);
		this.proxyInterface = (Class<? extends CometSubscriber>) mosContext
				.loadClass(proxyInterfaceName);

		// Provide meta-data
		context.setObjectClass(this.proxyInterface);
		context.setManagedObjectClass(CometProxyPublisherManagedObject.class);
		context.addDependency(Dependencies.COMET_PUBLISHER,
				CometPublisher.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new CometProxyPublisherManagedObject();
	}

	/**
	 * {@link ManagedObject} to provide the {@link Proxy} from the
	 * {@link CometPublisher}.
	 */
	private class CometProxyPublisherManagedObject implements
			CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link Proxy} from the {@link CometPublisher}.
		 */
		private Object proxy;

		/*
		 * ===================== ManagedObject ========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {

			// Obtain the comet publisher
			CometPublisher publisher = (CometPublisher) registry
					.getObject(Dependencies.COMET_PUBLISHER);

			// Create the proxy
			this.proxy = publisher
					.createPublisher(CometProxyPublisherManagedObjectSource.this.proxyInterface);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.proxy;
		}
	}

}