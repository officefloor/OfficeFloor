/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.api.managedobject.source.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.structure.Flow;

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
	 * @param context Specifications.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for the {@link AbstractAsyncManagedObjectSource#getSpecification()}.
	 */
	public static interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name  Name of property.
		 * @param label Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property {@link ManagedObjectSourceProperty}.
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

	@Override
	public ManagedObjectSourceMetaData<O, F> init(ManagedObjectSourceContext<F> context) throws Exception {

		// Create the meta-data
		MetaData metaData = new MetaData(context);

		// Initialise the meta-data
		this.loadMetaData(metaData);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Overridden to load meta-data.
	 * 
	 * @param context Meta-data.
	 * @throws Exception If fails to load the meta-data.
	 */
	protected abstract void loadMetaData(MetaDataContext<O, F> context) throws Exception;

	/**
	 * Context for the {@link ManagedObjectSource#init(ManagedObjectSourceContext)}.
	 */
	public static interface MetaDataContext<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * Obtains the {@link ManagedObjectSourceContext}.
		 * 
		 * @return {@link ManagedObjectSourceContext}.
		 */
		ManagedObjectSourceContext<F> getManagedObjectSourceContext();

		/**
		 * Specifies the type of the object returned from the {@link ManagedObject}.
		 * 
		 * @param objectClass Object type.
		 */
		void setObjectClass(Class<?> objectClass);

		/**
		 * Specifies the type of the {@link ManagedObject}.
		 * 
		 * @param managedObjectClass {@link ManagedObject} type.
		 */
		void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass);

		/**
		 * Adds a required dependency identified by the key.
		 * 
		 * @param key            {@link Enum} to identify the dependency.
		 * @param dependencyType Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required dependency.
		 */
		DependencyLabeller<O> addDependency(O key, Class<?> dependencyType);

		/**
		 * Adds a required dependency identified by an index into the order the
		 * dependency was added.
		 * 
		 * @param dependencyType Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required dependency.
		 */
		DependencyLabeller<O> addDependency(Class<?> dependencyType);

		/**
		 * Adds a required {@link Flow} identified by the key.
		 * 
		 * @param key          {@link Enum} to identify the {@link Flow}.
		 * @param argumentType Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller<F> addFlow(F key, Class<?> argumentType);

		/**
		 * Adds a required {@link Flow} identified by an index into the order the
		 * {@link Flow} was added.
		 * 
		 * @param argumentType Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller<F> addFlow(Class<?> argumentType);

		/**
		 * Adds a required {@link ExecutionStrategy} identified by an index into the
		 * order the {@link ExecutionStrategy} was added.
		 * 
		 * @return {@link ExecutionLabeller} to possibly label the
		 *         {@link ExecutionStrategy}.
		 */
		ExecutionLabeller addExecutionStrategy();

		/**
		 * Adds a {@link ManagedObjectExtensionMetaData} instance.
		 * 
		 * @param <E>                       Extension interface type.
		 * @param interfaceType             Type of the extension interface supported by
		 *                                  the {@link ManagedObject} instances.
		 * @param extensionInterfaceFactory {@link ExtensionFactory}.
		 */
		<E> void addManagedObjectExtension(Class<E> interfaceType, ExtensionFactory<E> extensionInterfaceFactory);
	}

	/**
	 * Provide {@link Labeller} functionality along with qualifying type of
	 * dependency.
	 */
	public static interface DependencyLabeller<K extends Enum<K>> extends Labeller<K> {

		/**
		 * Specifies qualifier for the type.
		 * 
		 * @param qualifier Type qualifier.
		 * @return <code>this</code> {@link Labeller} (allows simpler coding).
		 */
		Labeller<K> setTypeQualifier(String qualifier);

		/**
		 * Adds an annotation for the type.
		 * 
		 * @param annotation Annotation.
		 * @return <code>this</code>.
		 */
		Labeller<K> addAnnotation(Object annotation);
	}

	/**
	 * Provides the ability to label the required dependency or {@link Flow}.
	 */
	public static interface Labeller<K extends Enum<K>> {

		/**
		 * Specifies the label.
		 * 
		 * @param label Label.
		 * @return <code>this</code> {@link Labeller} (allows simpler coding).
		 */
		Labeller<K> setLabel(String label);

		/**
		 * Obtains the key of the dependency or {@link Flow}.
		 * 
		 * @return Key of the dependency or {@link Flow}. May be <code>null</code> if
		 *         {@link Indexed}.
		 */
		K getKey();

		/**
		 * Obtains the index of the dependency or {@link Flow}.
		 * 
		 * @return Index of the dependency or {@link Flow}.
		 */
		int getIndex();
	}

	/**
	 * Provides the ability to label the required {@link ExecutionStrategy}.
	 */
	public static interface ExecutionLabeller {

		/**
		 * Specifies the label.
		 * 
		 * @param label Label.
		 * @return <code>this</code> {@link ExecutionLabeller} (allows simpler coding).
		 */
		ExecutionLabeller setLabel(String label);

		/**
		 * Obtains the index of the {@link ExecutionStrategy}.
		 * 
		 * @return Index of the {@link ExecutionStrategy}.
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
		private Map<Integer, ManagedObjectDependencyMetaData<O>> dependencies = new HashMap<>();

		/**
		 * {@link ManagedObjectFlowMetaData} instances.
		 */
		private Map<Integer, ManagedObjectFlowMetaData<F>> flows = new HashMap<>();

		/**
		 * {@link ManagedObjectExecutionMetaData} instances.
		 */
		private Map<Integer, ManagedObjectExecutionMetaData> executions = new HashMap<>();

		/**
		 * {@link ManagedObjectExtensionMetaData} instances.
		 */
		private List<ManagedObjectExtensionMetaData<?>> externsionInterfaces = new LinkedList<>();

		/**
		 * Initiate.
		 * 
		 * @param context {@link ManagedObjectSourceContext}.
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
		public DependencyLabeller<O> addDependency(O key, Class<?> dependencyType) {
			// Use ordinal of key to index the dependency
			return this.addDependency(key.ordinal(), key, dependencyType);
		}

		@Override
		public DependencyLabeller<O> addDependency(Class<?> dependencyType) {
			// Indexed, so use next index (size will increase with indexing)
			return this.addDependency(this.dependencies.size(), null, dependencyType);
		}

		/**
		 * Adds a dependency.
		 * 
		 * @param index          Index to add the dependency under.
		 * @param key            Key for the dependency. May be <code>null</code>.
		 * @param dependencyType Type of dependency.
		 * @return {@link Labeller} for the dependency.
		 */
		private DependencyLabeller<O> addDependency(final int index, final O key, Class<?> dependencyType) {

			// Create the dependency meta-data
			final ManagedObjectDependencyMetaDataImpl<O> dependency = new ManagedObjectDependencyMetaDataImpl<O>(key,
					dependencyType);

			// Register the dependency at the index
			this.dependencies.put(Integer.valueOf(index), dependency);

			// Return the labeller for the dependency
			return new DependencyLabeller<O>() {
				@Override
				public Labeller<O> setLabel(String label) {
					dependency.setLabel(label);
					return this;
				}

				@Override
				public Labeller<O> setTypeQualifier(String qualifier) {
					dependency.setTypeQualifier(qualifier);
					return this;
				}

				@Override
				public O getKey() {
					return key;
				}

				@Override
				public int getIndex() {
					return index;
				}

				@Override
				public Labeller<O> addAnnotation(Object annotation) {
					dependency.addAnnotation(annotation);
					return this;
				}
			};
		}

		@Override
		public Labeller<F> addFlow(F key, Class<?> argumentType) {
			// Use ordinal of key to index the flow
			return this.addFlow(key.ordinal(), key, argumentType);
		}

		@Override
		public Labeller<F> addFlow(Class<?> argumentType) {
			// Indexed, so use next index (size will increase with indexing)
			return this.addFlow(this.flows.size(), null, argumentType);
		}

		/**
		 * Adds a {@link Flow}.
		 * 
		 * @param index        Index to add the {@link Flow} under.
		 * @param key          Key for the {@link Flow}. May be <code>null</code>.
		 * @param argumentType Type of the argument passed to the {@link Flow}.
		 * @return {@link Labeller} for the {@link Flow}.
		 */
		private Labeller<F> addFlow(final int index, final F key, Class<?> argumentType) {

			// Create the flow meta-data
			final ManagedObjectFlowMetaDataImpl<F> flow = new ManagedObjectFlowMetaDataImpl<F>(key, argumentType);

			// Register the flow at the index
			this.flows.put(Integer.valueOf(index), flow);

			// Return the labeller for the flow
			return new Labeller<F>() {
				@Override
				public Labeller<F> setLabel(String label) {
					flow.setLabel(label);
					return this;
				}

				@Override
				public F getKey() {
					return key;
				}

				@Override
				public int getIndex() {
					return index;
				}
			};
		}

		@Override
		public ExecutionLabeller addExecutionStrategy() {

			// Create the execution meta-data
			final ManagedObjectExecutionMetaDataImpl execution = new ManagedObjectExecutionMetaDataImpl();

			// Register the execution at the index
			int index = this.executions.size();
			this.executions.put(Integer.valueOf(index), execution);

			// Return the labeller for the flow
			return new ExecutionLabeller() {

				@Override
				public ExecutionLabeller setLabel(String label) {
					execution.setLabel(label);
					return this;
				}

				@Override
				public int getIndex() {
					return index;
				}
			};
		}

		@Override
		public <E> void addManagedObjectExtension(Class<E> interfaceType,
				ExtensionFactory<E> extensionInterfaceFactory) {
			this.externsionInterfaces
					.add(new ManagedObjectExtensionMetaDataImpl<E>(interfaceType, extensionInterfaceFactory));
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
		public ManagedObjectExecutionMetaData[] getExecutionMetaData() {
			return ConstructUtil.toArray(this.executions, new ManagedObjectExecutionMetaData[0]);
		}

		@Override
		public ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData() {
			return this.externsionInterfaces.toArray(new ManagedObjectExtensionMetaData[0]);
		}
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
