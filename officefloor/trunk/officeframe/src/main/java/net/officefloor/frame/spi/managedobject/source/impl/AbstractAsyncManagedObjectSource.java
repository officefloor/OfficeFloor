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
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
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
public abstract class AbstractAsyncManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource<D, H> {

	/*
	 * ================ ManagedObjectSource ===========================
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
	private class Specification implements SpecificationContext,
			ManagedObjectSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ManagedObjectSourceProperty> properties = new LinkedList<ManagedObjectSourceProperty>();

		/*
		 * ========== SpecificationContext ========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties
					.add(new ManagedObjectSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties
					.add(new ManagedObjectSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ManagedObjectSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ========== ManagedObjectSourceSpecification ===========
		 */

		@Override
		public ManagedObjectSourceProperty[] getProperties() {
			return this.properties.toArray(new ManagedObjectSourceProperty[0]);
		}
	}

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private MetaData metaData = null;

	@Override
	public void init(ManagedObjectSourceContext<H> context) throws Exception {

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
	protected abstract void loadMetaData(MetaDataContext<D, H> context)
			throws Exception;

	/**
	 * Context for the {@link ManagedObjectSource#getMetaData()}.
	 */
	public static interface MetaDataContext<D extends Enum<D>, H extends Enum<H>> {

		/**
		 * Obtains the {@link ManagedObjectSourceContext}.
		 * 
		 * @return {@link ManagedObjectSourceContext}.
		 */
		ManagedObjectSourceContext<H> getManagedObjectSourceContext();

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
		DependencyLoader<D> getDependencyLoader(Class<D> keys);

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
		HandlerLoader<H> getHandlerLoader(Class<H> keys);

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
	private class MetaData implements MetaDataContext<D, H>,
			DependencyLoader<D>, HandlerLoader<H>,
			ManagedObjectSourceMetaData<D, H> {

		/**
		 * {@link ManagedObjectSourceContext}.
		 */
		private final ManagedObjectSourceContext<H> context;

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
		public MetaData(ManagedObjectSourceContext<H> context) {
			this.context = context;
		}

		/*
		 * ============ MetaDataContext ==========================
		 */

		@Override
		public ManagedObjectSourceContext<H> getManagedObjectSourceContext() {
			return this.context;
		}

		@Override
		public void setObjectClass(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		@Override
		public void setManagedObjectClass(
				Class<? extends ManagedObject> managedObjectClass) {
			this.managedObjectClass = managedObjectClass;
		}

		@Override
		public DependencyLoader<D> getDependencyLoader(Class<D> keys) {

			// Specify details
			this.dependencyKeys = keys;
			this.dependencyTypes = new EnumMap<D, Class<?>>(this.dependencyKeys);

			// Return this to allow loading dependencies
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public HandlerLoader<H> getHandlerLoader(Class<H> keys) {

			// Specify details
			this.handlerKeys = keys;
			this.handlerTypes = new EnumMap<H, Class<? extends Handler>>(
					this.handlerKeys);

			// Return this to allow loading handlers
			return this;
		}

		@Override
		public <I> void addManagedObjectExtensionInterface(
				Class<I> interfaceType,
				ExtensionInterfaceFactory<I> extensionInterfaceFactory) {
			this.externsionInterfaces
					.add(new ManagedObjectExtensionInterfaceMetaDataImpl<I>(
							interfaceType, extensionInterfaceFactory));
		}

		/*
		 * ============== DependencyLoader =========================
		 */

		@Override
		public void mapDependencyType(D key, Class<?> type) {
			this.dependencyTypes.put(key, type);
		}

		/*
		 * ============== HandlerLoader =============================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void mapHandlerType(H key, Class<? extends Handler> type) {
			this.handlerTypes.put(key, type);
		}

		/*
		 * ============== ManagedObjectSourceMetaData ===============
		 */

		@Override
		public Class<?> getObjectClass() {
			return this.objectClass;
		}

		@Override
		public Class<? extends ManagedObject> getManagedObjectClass() {
			return this.managedObjectClass;
		}

		@Override
		public Class<D> getDependencyKeys() {
			return this.dependencyKeys;
		}

		@Override
		public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
			return new ManagedObjectDependencyMetaDataImpl(this.dependencyTypes
					.get(key));
		}

		@Override
		public Class<H> getHandlerKeys() {
			return this.handlerKeys;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends Handler> getHandlerType(H key) {
			return this.handlerTypes.get(key);
		}

		@Override
		public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
			return this.externsionInterfaces
					.toArray(new ManagedObjectExtensionInterfaceMetaData[0]);
		}

	}

	@Override
	public ManagedObjectSourceMetaData<D, H> getMetaData() {
		return this.metaData;
	}

	@Override
	public void start(final ManagedObjectExecuteContext<H> context)
			throws Exception {
		// Invoke start
		this.start(new StartContext<H>() {
			@Override
			public ManagedObjectExecuteContext<H> getContext(
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
				return context;
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
	protected void start(StartContext<H> startContext) throws Exception {
		// Do nothing by default
	}

	/**
	 * Context for
	 * {@link AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)}
	 * .
	 */
	public static interface StartContext<H extends Enum<H>> {

		/**
		 * Obtains the {@link ManagedObjectExecuteContext}.
		 * 
		 * @param handlerKeys
		 *            Keys for the {@link Handler} instances that <b>MUST</b>
		 *            match {@link ManagedObjectSourceMetaData#getHandlerKeys()}
		 *            .
		 * @return {@link ManagedObjectExecuteContext}.
		 */
		ManagedObjectExecuteContext<H> getContext(Class<H> handlerKeys);
	}

	/**
	 * {@link #sourceManagedObject(ManagedObjectUser)} to be implemented.
	 */

}
