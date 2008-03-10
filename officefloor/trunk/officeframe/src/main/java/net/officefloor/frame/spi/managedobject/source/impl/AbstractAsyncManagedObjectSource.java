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
package net.officefloor.frame.spi.managedobject.source.impl;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Abstract {@link ManagedObjectSource} allowing asynchronous sourcing of a
 * {@link ManagedObject}.
 * 
 * @author Daniel
 */
public abstract class AbstractAsyncManagedObjectSource
		implements
		ManagedObjectSource<AbstractAsyncManagedObjectSource.DummyKey, AbstractAsyncManagedObjectSource.DummyKey> {

	/**
	 * Dummy keys for the dependency and {@link Handler} instances.
	 */
	private static enum DummyKey {
	}

	/*
	 * ================================================================
	 * ManagedObjectSource
	 * ================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	@Override
	public ManagedObjectSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specifications.
	 * 
	 * @param context
	 *            Specifications.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for the
	 * {@link AbstractAsyncManagedObjectSource#getSpecification()}.
	 */
	public static interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property.
		 * @param label
		 *            Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property
		 *            {@link ManagedObjectSourceProperty}.
		 */
		void addProperty(ManagedObjectSourceProperty property);
	}

	/**
	 * Specification for this {@link ManagedObjectSource}.
	 */
	private static class Specification implements SpecificationContext,
			ManagedObjectSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ManagedObjectSourceProperty> properties = new LinkedList<ManagedObjectSourceProperty>();

		/*
		 * =======================================================
		 * SpecificationContext
		 * =======================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.SpecificationContext#addProperty(java.lang.String)
		 */
		@Override
		public void addProperty(String name) {
			this.properties
					.add(new ManagedObjectSourcePropertyImpl(name, name));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.SpecificationContext#addProperty(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void addProperty(String name, String label) {
			this.properties
					.add(new ManagedObjectSourcePropertyImpl(name, label));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.SpecificationContext#addProperty(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty)
		 */
		@Override
		public void addProperty(ManagedObjectSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =======================================================
		 * ManagedObjectSourceSpecification
		 * =======================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification#getProperties()
		 */
		@Override
		public ManagedObjectSourceProperty[] getProperties() {
			return this.properties.toArray(new ManagedObjectSourceProperty[0]);
		}
	}

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private MetaData<?, ?> metaData = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void init(ManagedObjectSourceContext context) throws Exception {

		// Create the meta-data
		this.metaData = new MetaData(context);

		// Initialise the meta-data
		this.loadMetaData(this.metaData);
	}

	/**
	 * Overridden to load meta-data.
	 * 
	 * @param context
	 *            Meta-data.
	 * @throws Exception
	 *             If fails to load the meta-data.
	 */
	protected abstract void loadMetaData(MetaDataContext context)
			throws Exception;

	/**
	 * Context for the {@link ManagedObjectSource#getMetaData()}.
	 */
	public static interface MetaDataContext {

		/**
		 * Obtains the {@link ManagedObjectSourceContext}.
		 * 
		 * @return {@link ManagedObjectSourceContext}.
		 */
		ManagedObjectSourceContext getManagedObjectSourceContext();

		/**
		 * Specifies the type of the object returned from the
		 * {@link ManagedObject}.
		 * 
		 * @param objectClass
		 *            Object type.
		 */
		void setObjectClass(Class<?> objectClass);

		/**
		 * Specifies the type of the {@link ManagedObject}.
		 * 
		 * @param managedObjectClass
		 *            {@link ManagedObject} type.
		 */
		void setManagedObjectClass(
				Class<? extends ManagedObject> managedObjectClass);

		/**
		 * <p>
		 * Obtains the {@link DependencyLoader}.
		 * <p>
		 * Calling this overwrites the previous dependencies loaded.
		 * 
		 * @param keys
		 *            {@link Enum} specifying the dependency keys.
		 * @return {@link DependencyLoader}.
		 */
		<D extends Enum<D>> DependencyLoader<D> getDependencyLoader(
				Class<D> keys);

		/**
		 * <p>
		 * Obtains the {@link HandlerLoader}.
		 * <p>
		 * Calling this overwrites the previous handlers loaded.
		 * 
		 * @param keys
		 *            {@link Enum} specifying the handler keys.
		 * @return {@link HandlerLoader}.
		 */
		<H extends Enum<H>> HandlerLoader<H> getHandlerLoader(Class<H> keys);

		/**
		 * Adds a {@link ManagedObjectExtensionInterfaceMetaData} instance.
		 * 
		 * @param interfaceType
		 *            Interface type.
		 * @param extensionInterfaceFactory
		 *            {@link ExtensionInterfaceFactory}.
		 */
		<I> void addManagedObjectExtensionInterface(Class<I> interfaceType,
				ExtensionInterfaceFactory<I> extensionInterfaceFactory);
	}

	/**
	 * Dependency loader.
	 */
	public static interface DependencyLoader<D extends Enum<D>> {

		/**
		 * Maps in the dependency type for the key.
		 * 
		 * @param key
		 *            Dependency key.
		 * @param type
		 *            Type mapped to the dependency.
		 */
		void mapDependencyType(D key, Class<?> type);
	}

	/**
	 * Handler loader.
	 */
	public static interface HandlerLoader<H extends Enum<H>> {

		/**
		 * Maps in the handler type for the key.
		 * 
		 * @param key
		 *            Handler key.
		 * @param type
		 *            Type mapped to the handler.
		 */
		@SuppressWarnings("unchecked")
		void mapHandlerType(H key, Class<? extends Handler> type);
	}

	/**
	 * Meta-data for the {@link ManagedObjectSource}.
	 */
	private static class MetaData<D extends Enum<D>, H extends Enum<H>>
			implements MetaDataContext, DependencyLoader<D>, HandlerLoader<H>,
			ManagedObjectSourceMetaData<D, H> {

		/**
		 * {@link ManagedObjectSourceContext}.
		 */
		private final ManagedObjectSourceContext context;

		/**
		 * Object {@link Class}.
		 */
		private Class<?> objectClass;

		/**
		 * {@link ManagedObject} {@link Class} that is defaulted to
		 * {@link ManagedObject}.
		 */
		private Class<? extends ManagedObject> managedObjectClass = ManagedObject.class;

		/**
		 * {@link Enum} specifying dependency keys.
		 */
		private Class<D> dependencyKeys = null;

		/**
		 * Types for each dependency key.
		 */
		private Map<D, Class<?>> dependencyTypes = null;

		/**
		 * {@link Enum} specifying handler keys.
		 */
		private Class<H> handlerKeys = null;

		/**
		 * Handler for each handler key.
		 */
		@SuppressWarnings("unchecked")
		private Map<H, Class<? extends Handler>> handlerTypes = null;

		/**
		 * {@link ManagedObjectExtensionInterfaceMetaData} instances.
		 */
		private List<ManagedObjectExtensionInterfaceMetaData<?>> externsionInterfaces = new LinkedList<ManagedObjectExtensionInterfaceMetaData<?>>();

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link ManagedObjectSourceContext}.
		 */
		public MetaData(ManagedObjectSourceContext context) {
			this.context = context;
		}

		/*
		 * =======================================================
		 * MetaDataContext
		 * =======================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#getManagedObjectSourceContext()
		 */
		@Override
		public ManagedObjectSourceContext getManagedObjectSourceContext() {
			return this.context;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#setObjectClass(java.lang.Class)
		 */
		@Override
		public void setObjectClass(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#setManagedObjectClass(java.lang.Class)
		 */
		@Override
		public void setManagedObjectClass(
				Class<? extends ManagedObject> managedObjectClass) {
			this.managedObjectClass = managedObjectClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#getDependencyLoader(java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <T extends Enum<T>> DependencyLoader<T> getDependencyLoader(
				Class<T> keys) {

			// Specify details
			this.dependencyKeys = (Class<D>) keys;
			this.dependencyTypes = new EnumMap<D, Class<?>>(this.dependencyKeys);

			// Return this to allow loading dependencies
			return (DependencyLoader<T>) this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#getHandlerLoader(java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <T extends Enum<T>> HandlerLoader<T> getHandlerLoader(
				Class<T> keys) {

			// Specify details
			this.handlerKeys = (Class<H>) keys;
			this.handlerTypes = new EnumMap<H, Class<? extends Handler>>(
					this.handlerKeys);

			// Return this to allow loading handlers
			return (HandlerLoader<T>) this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.MetaDataContext#addManagedObjectExtensionInterface(java.lang.Class,
		 *      net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory)
		 */
		@Override
		public <I> void addManagedObjectExtensionInterface(
				Class<I> interfaceType,
				ExtensionInterfaceFactory<I> extensionInterfaceFactory) {
			this.externsionInterfaces
					.add(new ManagedObjectExtensionInterfaceMetaDataImpl<I>(
							interfaceType, extensionInterfaceFactory));
		}

		/*
		 * =======================================================
		 * DependencyLoader
		 * =======================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.DependencyLoader#mapDependencyType(java.lang.Enum,
		 *      java.lang.Class)
		 */
		@Override
		public void mapDependencyType(D key, Class<?> type) {
			this.dependencyTypes.put(key, type);
		}

		/*
		 * ============================================================
		 * HandlerLoader
		 * ============================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource.HandlerLoader#mapHandlerType(java.lang.Enum,
		 *      java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void mapHandlerType(H key, Class<? extends Handler> type) {
			this.handlerTypes.put(key, type);
		}

		/*
		 * =======================================================
		 * ManagedObjectSourceMetaData
		 * =======================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
		 */
		@Override
		public Class<?> getObjectClass() {
			return this.objectClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
		 */
		@Override
		public Class<? extends ManagedObject> getManagedObjectClass() {
			return this.managedObjectClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
		 */
		@Override
		public Class<D> getDependencyKeys() {
			return this.dependencyKeys;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(java.lang.Enum)
		 */
		@Override
		public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
			return new ManagedObjectDependencyMetaDataImpl(this.dependencyTypes
					.get(key));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
		 */
		@Override
		public Class<H> getHandlerKeys() {
			return this.handlerKeys;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(java.lang.Enum)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends Handler> getHandlerType(H key) {
			return this.handlerTypes.get(key);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
		 */
		@Override
		public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
			return this.externsionInterfaces
					.toArray(new ManagedObjectExtensionInterfaceMetaData[0]);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ManagedObjectSourceMetaData<DummyKey, DummyKey> getMetaData() {
		return (ManagedObjectSourceMetaData<DummyKey, DummyKey>) this.metaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	@Override
	public void start(final ManagedObjectExecuteContext<DummyKey> context)
			throws Exception {
		// Invoke start
		this.start(new StartContext() {
			@Override
			@SuppressWarnings("unchecked")
			public <H extends Enum<H>> ManagedObjectExecuteContext<H> getContext(
					Class<H> handlerKeys) {

				// Ensure the same handler keys
				if (handlerKeys != AbstractAsyncManagedObjectSource.this
						.getMetaData().getHandlerKeys()) {
					throw new IllegalStateException(
							"Incorrect handler keys [expecting "
									+ AbstractAsyncManagedObjectSource.this
											.getMetaData().getHandlerKeys()
									+ ", actual " + handlerKeys + "]");
				}

				// Return the execute context
				return (ManagedObjectExecuteContext<H>) context;
			}
		});
	}

	/**
	 * Override to provide start functionality.
	 * 
	 * @param startContext
	 *            {@link StartContext}.
	 * @throws Exception
	 *             If fails to start.
	 */
	protected void start(StartContext startContext) throws Exception {
		// Do nothing by default
	}

	/**
	 * Context for
	 * {@link AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)}.
	 */
	public static interface StartContext {

		/**
		 * Obtains the {@link ManagedObjectExecuteContext}.
		 * 
		 * @param handlerKeys
		 *            Keys for the {@link Handler} instances that <b>MUST</b>
		 *            match {@link ManagedObjectSourceMetaData#getHandlerKeys()}.
		 * @return {@link ManagedObjectExecuteContext}.
		 */
		<H extends Enum<H>> ManagedObjectExecuteContext<H> getContext(
				Class<H> handlerKeys);
	}

	/**
	 * {@link #sourceManagedObject(ManagedObjectUser)} to be implemented.
	 */

}
