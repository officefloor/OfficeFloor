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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Implements the {@link ManagedObjectBuilder}.
 * 
 * @author Daniel
 */
public class ManagedObjectBuilderImpl<D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>>
		implements ManagedObjectBuilder<H>,
		ManagedObjectSourceConfiguration<H, MS> {

	/**
	 * Name of {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	private final Class<MS> managedObjectSourceClass;

	/**
	 * Name of {@link Office} managing this {@link ManagedObject}.
	 */
	private String managingOfficeName;

	/**
	 * {@link Properties} for the {@link ManagedObjectSource}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link ManagedObjectPool}.
	 */
	private ManagedObjectPool pool;

	/**
	 * Default timeout for asynchronous operations on the {@link ManagedObject}.
	 */
	private long defaultTimeout = 0;

	/**
	 * {@link ManagedObjectHandlerBuilder} implementation.
	 */
	private final ManagedObjectHandlerBuilderImpl handlersBuilder = new ManagedObjectHandlerBuilderImpl();

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            {@link Class} of the {@link ManagedObjectSource}.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName,
			Class<MS> managedObjectSourceClass) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/*
	 * ================= ManagedObjectBuilder =============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.put(name, value);
	}

	@Override
	public void setManagedObjectPool(ManagedObjectPool pool) {
		this.pool = pool;
	}

	@Override
	public void setDefaultTimeout(long timeout) {
		this.defaultTimeout = timeout;
	}

	@Override
	public void setManagingOffice(String officeName) {
		this.managingOfficeName = officeName;
	}

	@Override
	public ManagedObjectHandlerBuilder<H> getManagedObjectHandlerBuilder() {
		return this.handlersBuilder;
	}

	/*
	 * ================= ManagedObjectSourceConfiguration =================
	 */

	@Override
	public String getManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public ManagedObjectBuilder<H> getBuilder() {
		return this;
	}

	@Override
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	@Override
	public Class<MS> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	@Override
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	@Override
	public ManagedObjectHandlerBuilder<H> getHandlerBuilder() {
		return this.getManagedObjectHandlerBuilder();
	}

	@Override
	public HandlerConfiguration<H, ?>[] getHandlerConfiguration() {
		return this.handlersBuilder.getHandlerConfiguration();
	}

	/**
	 * {@link ManagedObjectHandlerBuilder} implementation.
	 */
	protected class ManagedObjectHandlerBuilderImpl implements
			ManagedObjectHandlerBuilder<H> {

		/**
		 * Handlers.
		 */
		private final Map<H, HandlerBuilderImpl<H, ?>> handlers;

		/**
		 * Initiate.
		 */
		public ManagedObjectHandlerBuilderImpl() {
			this.handlers = new HashMap<H, HandlerBuilderImpl<H, ?>>();
		}

		/**
		 * Obtains the {@link HandlerConfiguration} instances.
		 * 
		 * @return {@link HandlerConfiguration} instances.
		 */
		@SuppressWarnings("unchecked")
		public HandlerConfiguration<H, ?>[] getHandlerConfiguration() {

			// Obtain the size of array
			int arraySize = -1;
			for (H handlerKey : this.handlers.keySet()) {
				int handlerIndex = handlerKey.ordinal();
				if (handlerIndex > arraySize) {
					arraySize = handlerIndex;
				}
			}
			arraySize += 1; // size is max index + 1

			// Create the listing of handler configurations
			HandlerConfiguration<H, ?>[] handlerConfigurations = new HandlerConfiguration[arraySize];
			for (H handlerKey : this.handlers.keySet()) {

				// Obtain the handler configuration
				HandlerConfiguration<H, ?> handlerConfiguration = this.handlers
						.get(handlerKey);
				if (handlerConfiguration == null) {

					// Ensure handler configuration available
					handlerConfiguration = new HandlerBuilderImpl(handlerKey,
							Indexed.class);
				}

				// Provide at ordinal position for the key
				handlerConfigurations[handlerKey.ordinal()] = handlerConfiguration;
			}

			// Return the handler configuration
			return handlerConfigurations;
		}

		/*
		 * ================ ManagedObjectHandlerBuilder =====================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.api.build.ManagedObjectHandlersBuilder#
		 * registerHandler(java.lang.Enum)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public HandlerBuilder<Indexed> registerHandler(H handlerKey) {

			// Obtain the handler builder
			HandlerBuilderImpl handlerBuilder = this.handlers.get(handlerKey);
			if (handlerBuilder == null) {
				// Create the handler builder
				handlerBuilder = new HandlerBuilderImpl<H, Indexed>(handlerKey,
						null);

				// Register the handler
				this.handlers.put(handlerKey, handlerBuilder);
			}

			// Return the builder
			return (HandlerBuilder<Indexed>) handlerBuilder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.api.build.ManagedObjectHandlerBuilder#
		 * registerHandler(java.lang.Enum, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F>> HandlerBuilder<F> registerHandler(
				H handlerKey, Class<F> processListingEnum) {

			// Obtain the handler builder
			HandlerBuilderImpl<H, ?> handlerBuilder = this.handlers
					.get(handlerKey);
			if (handlerBuilder == null) {
				// Create the handler builder
				handlerBuilder = new HandlerBuilderImpl<H, F>(handlerKey,
						processListingEnum);

				// Register the handler
				this.handlers.put(handlerKey, handlerBuilder);
			}

			// Return the builder
			return (HandlerBuilder<F>) handlerBuilder;
		}
	}

}
