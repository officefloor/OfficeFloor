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
package net.officefloor.frame.spi.managedobject.source.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Abstract {@link ManagedObjectSource} allowing to asynchronously source the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAsyncManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
		implements ManagedObjectSource<O, F> {

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
	private class Specification implements SpecificationContext, ManagedObjectSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ManagedObjectSourceProperty> properties = new LinkedList<ManagedObjectSourceProperty>();

		/*
		 * ========== SpecificationContext ========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new ManagedObjectSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new ManagedObjectSourcePropertyImpl(name, label));
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
	public void init(ManagedObjectSourceContext<F> context) throws Exception {

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
	protected abstract void loadMetaData(MetaDataContext<O, F> context) throws Exception;

	/**
	 * Context for the {@link ManagedObjectSource#getMetaData()}.
	 */
	public static interface MetaDataContext<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * Obtains the {@link ManagedObjectSourceContext}.
		 * 
		 * @return {@link ManagedObjectSourceContext}.
		 */
		ManagedObjectSourceContext<F> getManagedObjectSourceContext();

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
		void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass);

		/**
		 * Adds a required dependency identified by the key.
		 * 
		 * @param key
		 *            {@link Enum} to identify the dependency.
		 * @param dependencyType
		 *            Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required
		 *         dependency.
		 */
		DependencyLabeller addDependency(O key, Class<?> dependencyType);

		/**
		 * Adds a required dependency identified by an index into the order the
		 * dependency was added.
		 * 
		 * @param dependencyType
		 *            Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required
		 *         dependency.
		 */
		DependencyLabeller addDependency(Class<?> dependencyType);

		/**
		 * Adds a required {@link Flow} identified by the key.
		 * 
		 * @param key
		 *            {@link Enum} to identify the {@link Flow}.
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller addFlow(F key, Class<?> argumentType);

		/**
		 * Adds a required {@link Flow} identified by an index into the order
		 * the {@link Flow} was added.
		 * 
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller addFlow(Class<?> argumentType);

		/**
		 * Adds a {@link ManagedObjectExtensionInterfaceMetaData} instance.
		 * 
		 * @param <E>
		 *            Extension interface type.
		 * @param interfaceType
		 *            Type of the extension interface supported by the
		 *            {@link ManagedObject} instances.
		 * @param extensionInterfaceFactory
		 *            {@link ExtensionInterfaceFactory}.
		 */
		<E> void addManagedObjectExtensionInterface(Class<E> interfaceType,
				ExtensionInterfaceFactory<E> extensionInterfaceFactory);
	}

	/**
	 * Provide {@link Labeller} functionality along with qualifying type of
	 * dependency.
	 */
	public static interface DependencyLabeller extends Labeller {

		/**
		 * Specifies qualifier for the type.
		 * 
		 * @param qualifier
		 *            Type qualifier.
		 * @return <code>this</code> {@link Labeller} (allows simpler coding).
		 */
		Labeller setTypeQualifier(String qualifier);

	}

	/**
	 * Provides the ability to label the required dependency or {@link Flow}.
	 */
	public static interface Labeller {

		/**
		 * Specifies the label.
		 * 
		 * @param label
		 *            Label.
		 * @return <code>this</code> {@link Labeller} (allows simpler coding).
		 */
		Labeller setLabel(String label);

		/**
		 * Obtains the index of the dependency of {@link Flow}.
		 * 
		 * @return Index of the dependency of {@link Flow}.
		 */
		int getIndex();
	}

	/**
	 * Meta-data for the {@link ManagedObjectSource}.
	 */
	private class MetaData implements MetaDataContext<O, F>, ManagedObjectSourceMetaData<O, F> {

		/**
		 * {@link ManagedObjectSourceContext}.
		 */
		private final ManagedObjectSourceContext<F> context;

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
		 * {@link ManagedObjectDependencyMetaData} instances.
		 */
		private Map<Integer, ManagedObjectDependencyMetaData<O>> dependencies = new HashMap<Integer, ManagedObjectDependencyMetaData<O>>();

		/**
		 * {@link ManagedObjectFlowMetaData} instances.
		 */
		private Map<Integer, ManagedObjectFlowMetaData<F>> flows = new HashMap<Integer, ManagedObjectFlowMetaData<F>>();

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
		public MetaData(ManagedObjectSourceContext<F> context) {
			this.context = context;
		}

		/*
		 * ============ MetaDataContext ==========================
		 */

		@Override
		public ManagedObjectSourceContext<F> getManagedObjectSourceContext() {
			return this.context;
		}

		@Override
		public void setObjectClass(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		@Override
		public void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass) {
			this.managedObjectClass = managedObjectClass;
		}

		@Override
		public DependencyLabeller addDependency(O key, Class<?> dependencyType) {
			// Use ordinal of key to index the dependency
			return this.addDependency(key.ordinal(), key, dependencyType);
		}

		@Override
		public DependencyLabeller addDependency(Class<?> dependencyType) {
			// Indexed, so use next index (size will increase with indexing)
			return this.addDependency(this.dependencies.size(), null, dependencyType);
		}

		/**
		 * Adds a dependency.
		 * 
		 * @param index
		 *            Index to add the dependency under.
		 * @param key
		 *            Key for the dependency. May be <code>null</code>.
		 * @param dependencyType
		 *            Type of dependency.
		 * @return {@link Labeller} for the dependency.
		 */
		private DependencyLabeller addDependency(final int index, O key, Class<?> dependencyType) {

			// Create the dependency meta-data
			final ManagedObjectDependencyMetaDataImpl<O> dependency = new ManagedObjectDependencyMetaDataImpl<O>(key,
					dependencyType);

			// Register the dependency at the index
			this.dependencies.put(new Integer(index), dependency);

			// Return the labeller for the dependency
			return new DependencyLabeller() {
				@Override
				public Labeller setLabel(String label) {
					dependency.setLabel(label);
					return this;
				}

				@Override
				public Labeller setTypeQualifier(String qualifier) {
					dependency.setTypeQualifier(qualifier);
					return this;
				}

				@Override
				public int getIndex() {
					return index;
				}
			};
		}

		@Override
		public Labeller addFlow(F key, Class<?> argumentType) {
			// Use ordinal of key to index the flow
			return this.addFlow(key.ordinal(), key, argumentType);
		}

		@Override
		public Labeller addFlow(Class<?> argumentType) {
			// Indexed, so use next index (size will increase with indexing)
			return this.addFlow(this.flows.size(), null, argumentType);
		}

		/**
		 * Adds a {@link Flow}.
		 * 
		 * @param index
		 *            Index to add the {@link Flow} under.
		 * @param key
		 *            Key for the {@link Flow}. May be <code>null</code>.
		 * @param argumentType
		 *            Type of the argument passed to the {@link Flow}.
		 * @return {@link Labeller} for the {@link Flow}.
		 */
		private Labeller addFlow(final int index, F key, Class<?> argumentType) {

			// Create the flow meta-data
			final ManagedObjectFlowMetaDataImpl<F> flow = new ManagedObjectFlowMetaDataImpl<F>(key, argumentType);

			// Register the flow at the index
			this.flows.put(new Integer(index), flow);

			// Return the labeller for the flow
			return new Labeller() {
				@Override
				public Labeller setLabel(String label) {
					flow.setLabel(label);
					return this;
				}

				@Override
				public int getIndex() {
					return index;
				}
			};
		}

		@Override
		public <E> void addManagedObjectExtensionInterface(Class<E> interfaceType,
				ExtensionInterfaceFactory<E> extensionInterfaceFactory) {
			this.externsionInterfaces
					.add(new ManagedObjectExtensionInterfaceMetaDataImpl<E>(interfaceType, extensionInterfaceFactory));
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
		public ManagedObjectDependencyMetaData<O>[] getDependencyMetaData() {
			return ConstructUtil.toArray(this.dependencies, new ManagedObjectDependencyMetaData[0]);
		}

		@Override
		public ManagedObjectFlowMetaData<F>[] getFlowMetaData() {
			return ConstructUtil.toArray(this.flows, new ManagedObjectFlowMetaData[0]);
		}

		@Override
		public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
			return this.externsionInterfaces.toArray(new ManagedObjectExtensionInterfaceMetaData[0]);
		}
	}

	@Override
	public ManagedObjectSourceMetaData<O, F> getMetaData() {
		return this.metaData;
	}

	@Override
	public void start(ManagedObjectExecuteContext<F> context) throws Exception {
		// Override to provide start functionality
	}

	@Override
	public void stop() {
		// Override to provide stop functionality
	}

}