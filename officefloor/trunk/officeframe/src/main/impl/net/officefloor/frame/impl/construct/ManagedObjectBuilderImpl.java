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
package net.officefloor.frame.impl.construct;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlersBuilder;
import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Implements the {@link net.officefloor.frame.api.build.ManagedObjectBuilder}.
 * 
 * @author Daniel
 */
public class ManagedObjectBuilderImpl implements ManagedObjectBuilder,
		ManagedObjectSourceConfiguration {

	/**
	 * Name of {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private String managedObjectName;

	/**
	 * Name of {@link net.officefloor.frame.api.manage.Office} managing this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private String managingOfficeName;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	private Class managedObjectSourceClass;

	/**
	 * {@link Properties} for the {@link ManagedObjectSource}.
	 */
	private Properties properties = new Properties();

	/**
	 * {@link OfficeScope} for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private OfficeScope managedObjectScope = OfficeScope.WORK;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private ManagedObjectPool pool;

	/**
	 * Default timeout for asynchronous operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private long defaultTimeout = 0;

	/**
	 * {@link ManagedObjectHandlersBuilder} implementation.
	 */
	private ManagedObjectHandlersBuilderImpl<?> handlers = null;

	/**
	 * Specifies the name for this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param name
	 *            Name for this
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected void setManagedObjectName(String name) {
		this.managedObjectName = name;
	}

	/*
	 * ====================================================================
	 * ManagedObjectBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.managedobject.ManagedObjectMetaData#setManagedObjectSourceClass(java.lang.Class)
	 */
	public <S extends ManagedObjectSource> void setManagedObjectSourceClass(
			Class<S> managedObjectSourceClass) {
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.managedobject.ManagedObjectMetaData#addProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public void addProperty(String name, String value) {
		this.properties.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setManagedObjectPool(net.officefloor.frame.internal.structure.ManagedObjectPool)
	 */
	public void setManagedObjectPool(ManagedObjectPool pool) {
		this.pool = pool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setDefaultTimeout(long)
	 */
	public void setDefaultTimeout(long timeout) throws BuildException {
		this.defaultTimeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setManagingOffice(java.lang.String)
	 */
	public void setManagingOffice(String officeName) throws BuildException {
		this.managingOfficeName = officeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#getManagedObjectHandlerBuilder(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <H extends Enum<H>> ManagedObjectHandlersBuilder<H> getManagedObjectHandlerBuilder(
			Class<H> handlerKeys) throws BuildException {

		// Create the managed object handler builder
		this.handlers = new ManagedObjectHandlersBuilderImpl<H>(handlerKeys);

		// Return the builder
		return (ManagedObjectHandlersBuilder<H>) this.handlers;
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectName()
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagingOfficeName()
	 */
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectSourceClass()
	 */
	@SuppressWarnings("unchecked")
	public <MS extends ManagedObjectSource> Class<MS> getManagedObjectSourceClass()
			throws ConfigurationException {
		return this.managedObjectSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectScope()
	 */
	public OfficeScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectPool()
	 */
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getDefaultTimeout()
	 */
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getHandlerConfiguration()
	 */
	public HandlerConfiguration<?, ?>[] getHandlerConfiguration()
			throws ConfigurationException {
		// Return the handler configuration
		return (this.handlers == null ? new HandlerConfiguration<?, ?>[0]
				: this.handlers.getHandlerConfiguration());
	}

	/**
	 * {@link ManagedObjectHandlersBuilder} implementation.
	 */
	protected class ManagedObjectHandlersBuilderImpl<H extends Enum<H>>
			implements ManagedObjectHandlersBuilder<H> {

		/**
		 * {@link Enum} specifying the handler keys.
		 */
		private final Class<H> handlerKeys;

		/**
		 * Handlers.
		 */
		private final Map<H, HandlerBuilderImpl<H, ?>> handlers;

		/**
		 * Type that the {@link Handler} must implement.
		 */
		private Class<? extends Handler<?>> handlerType = null;

		/**
		 * Initiate.
		 * 
		 * @param handlerKeys
		 *            {@link Enum} providing the keys for the {@link Handler}
		 *            instances.
		 */
		public ManagedObjectHandlersBuilderImpl(Class<H> handlerKeys) {
			this.handlerKeys = handlerKeys;
			this.handlers = new EnumMap<H, HandlerBuilderImpl<H, ?>>(
					handlerKeys);
		}

		/**
		 * Obtains the {@link HandlerConfiguration} instances.
		 * 
		 * @return {@link HandlerConfiguration} instances.
		 * @throws ConfigurationException
		 *             If fails to obtain the {@link HandlerConfiguration}.
		 */
		@SuppressWarnings("unchecked")
		public HandlerConfiguration<?, ?>[] getHandlerConfiguration()
				throws ConfigurationException {

			// Create the listing of handler configurations
			H[] handlerKeys = this.handlerKeys.getEnumConstants();
			HandlerConfiguration<?, ?>[] handlerConfigurations = new HandlerConfiguration<?, ?>[handlerKeys.length];
			for (H handlerKey : handlerKeys) {

				// Obtain the handler configuration
				HandlerConfiguration<H, ?> handlerConfiguration = this.handlers
						.get(handlerKey);
				if (handlerConfiguration == null) {

					// No handler provided, therefore ensure type is specified
					if (this.handlerType == null) {
						throw new ConfigurationException(
								"No handler type provided for handler "
										+ handlerKey.name());
					}

					// Ensure handler configuration available
					handlerConfiguration = new HandlerBuilderImpl(handlerKey,
							Indexed.class, this.handlerType);
				}

				// Provide at ordinal position for the key
				handlerConfigurations[handlerKey.ordinal()] = handlerConfiguration;
			}

			// Return the handler configuration
			return handlerConfigurations;
		}

		/*
		 * ======================================================================
		 * ManagedObjectHandlersBuilder
		 * ======================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.ManagedObjectHandlersBuilder#registerHandler(java.lang.Enum)
		 */
		@Override
		public ManagedObjectHandlerBuilder registerHandler(H key)
				throws BuildException {
			return new ManagedObjectHandlerBuilderImpl(key);
		}

		/**
		 * {@link ManagedObjectHandlerBuilder} implementation.
		 */
		protected class ManagedObjectHandlerBuilderImpl implements
				ManagedObjectHandlerBuilder {

			/**
			 * Key identifying the {@link Handler}.
			 */
			private final H handlerKey;

			/**
			 * Initiate.
			 * 
			 * @param handlerKey
			 *            Key identifying the {@link Handler}.
			 */
			public ManagedObjectHandlerBuilderImpl(H handlerKey) {
				this.handlerKey = handlerKey;
			}

			/*
			 * ======================================================================
			 * ManagedObjectHandlerBuilder
			 * ======================================================================
			 */

			/*
			 * (non-Javadoc)
			 * 
			 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#setHandlerType(java.lang.Class)
			 */
			@Override
			public <HT extends Handler<?>> void setHandlerType(
					Class<HT> handlerType) throws BuildException {
				// Specify the handler type
				ManagedObjectHandlersBuilderImpl.this.handlerType = handlerType;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#getHandlerBuilder(java.lang.Class)
			 */
			@Override
			@SuppressWarnings("unchecked")
			public <F extends Enum<F>> HandlerBuilder<F> getHandlerBuilder(
					Class<F> processListingEnum) throws BuildException {

				// Create the handler builder
				HandlerBuilderImpl<H, F> handlerBuilder = new HandlerBuilderImpl(
						this.handlerKey, processListingEnum,
						ManagedObjectHandlersBuilderImpl.this.handlerType);

				// Register the handler
				ManagedObjectHandlersBuilderImpl.this.handlers.put(
						this.handlerKey, handlerBuilder);

				// Return the builder
				return handlerBuilder;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#getHandlerBuilder()
			 */
			@Override
			@SuppressWarnings("unchecked")
			public HandlerBuilder<Indexed> getHandlerBuilder()
					throws BuildException {

				// Create the handler builder
				HandlerBuilderImpl<H, Indexed> handlerBuilder = new HandlerBuilderImpl(
						this.handlerKey, null,
						ManagedObjectHandlersBuilderImpl.this.handlerType);

				// Register the handler
				ManagedObjectHandlersBuilderImpl.this.handlers.put(
						this.handlerKey, handlerBuilder);

				// Return the builder
				return handlerBuilder;
			}
		}

	}

}
