/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.pool.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceMetaData;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * Abstract {@link ManagedObjectPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedObjectPoolSource implements ManagedObjectPoolSource {

	/*
	 * ==================== ManagedObjectPoolSource ====================
	 */

	@Override
	public ManagedObjectPoolSourceSpecification getSpecification() {
		// Create and populate the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specification.
	 * 
	 * @param context
	 *            Specifications.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for the {@link AbstractManagedObjectPoolSource#getSpecification()}.
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
		 *            {@link ManagedObjectPoolSourceProperty}.
		 */
		void addProperty(ManagedObjectPoolSourceProperty property);
	}

	/**
	 * Specification for this {@link ManagedObjectPoolSource}.
	 */
	private class Specification implements SpecificationContext, ManagedObjectPoolSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ManagedObjectPoolSourceProperty> properties = new LinkedList<ManagedObjectPoolSourceProperty>();

		/*
		 * ================= SpecificationContext =======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new ManagedObjectPoolSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new ManagedObjectPoolSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ManagedObjectPoolSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =============== ManagedObjectPoolSourceSpecification ===============
		 */

		@Override
		public ManagedObjectPoolSourceProperty[] getProperties() {
			return this.properties.toArray(new ManagedObjectPoolSourceProperty[0]);
		}
	}

	@Override
	public ManagedObjectPoolSourceMetaData init(ManagedObjectPoolSourceContext context) throws Exception {
		// Create and populate the meta-data
		MetaData metaData = new MetaData(context);
		this.loadMetaData(metaData);
		return metaData;
	}

	/**
	 * Overridden to load meta-data.
	 * 
	 * @param context
	 *            Meta-data.
	 * @throws Exception
	 *             If fails to load the meta-data.
	 */
	protected abstract void loadMetaData(MetaDataContext context) throws Exception;

	/**
	 * Context for the
	 * {@link ManagedObjectPoolSource#init(ManagedObjectPoolSourceContext)}.
	 */
	public static interface MetaDataContext {

		/**
		 * Obtains the {@link ManagedObjectPoolSourceContext}.
		 * 
		 * @return {@link ManagedObjectPoolSourceContext}.
		 */
		ManagedObjectPoolSourceContext getManagedObjectPoolSourceContext();

		/**
		 * Specifies the pooled object type.
		 * 
		 * @param pooledObjectType
		 *            Pooled object type.
		 */
		void setPooledObjectType(Class<?> pooledObjectType);

		/**
		 * Specifies the {@link ManagedObjectPoolFactory}.
		 * 
		 * @param managedObjectPoolFactory
		 *            {@link ManagedObjectPoolFactory}.
		 */
		void setManagedObjectPoolFactory(ManagedObjectPoolFactory managedObjectPoolFactory);

		/**
		 * Adds a {@link ThreadCompletionListener}.
		 * 
		 * @param threadCompletionListenerFactory
		 *            {@link ThreadCompletionListenerFactory} for the
		 *            {@link ThreadCompletionListener}.
		 */
		void addThreadCompleteListener(ThreadCompletionListenerFactory threadCompletionListenerFactory);
	}

	/**
	 * Meta-data of the {@link ManagedObjectPoolSource}.
	 */
	private class MetaData implements MetaDataContext, ManagedObjectPoolSourceMetaData {

		/**
		 * {@link ManagedObjectPoolSourceContext}.
		 */
		private final ManagedObjectPoolSourceContext context;

		/**
		 * Pooled object type.
		 */
		private Class<?> pooledObjectType;

		/**
		 * {@link ManagedObjectPoolFactory}.
		 */
		private ManagedObjectPoolFactory managedObjectPoolFactory;

		/**
		 * {@link ThreadCompletionListenerFactory} instances.
		 */
		private final List<ThreadCompletionListenerFactory> threadCompletionListenerFactories = new LinkedList<>();

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link ManagedObjectPoolSourceContext}.
		 */
		private MetaData(ManagedObjectPoolSourceContext context) {
			this.context = context;
		}

		/*
		 * ================= MetaDataContext ================================
		 */

		@Override
		public ManagedObjectPoolSourceContext getManagedObjectPoolSourceContext() {
			return this.context;
		}

		@Override
		public void setPooledObjectType(Class<?> pooledObjectType) {
			this.pooledObjectType = pooledObjectType;
		}

		@Override
		public void setManagedObjectPoolFactory(ManagedObjectPoolFactory managedObjectPoolFactory) {
			this.managedObjectPoolFactory = managedObjectPoolFactory;
		}

		@Override
		public void addThreadCompleteListener(ThreadCompletionListenerFactory threadCompletionListenerFactory) {
			this.threadCompletionListenerFactories.add(threadCompletionListenerFactory);
		}

		/*
		 * ================= ManagedObjectPoolSourceMetaData =================
		 */

		@Override
		public Class<?> getPooledObjectType() {
			return this.pooledObjectType;
		}

		@Override
		public ManagedObjectPoolFactory getManagedObjectPoolFactory() {
			return this.managedObjectPoolFactory;
		}

		@Override
		public ThreadCompletionListenerFactory[] getThreadCompleteListenerFactories() {
			return this.threadCompletionListenerFactories.toArray(new ThreadCompletionListenerFactory[0]);
		}
	}

}
