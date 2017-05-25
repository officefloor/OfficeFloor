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
package net.officefloor.compile.spi.pool.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceMetaData;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;

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
	 * Context for the
	 * {@link AbstractManagedObjectPoolSource#getSpecification()}.
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

	/**
	 * {@link MetaData}.
	 */
	private MetaData metaData;

	@Override
	public ManagedObjectPoolSourceMetaData init(ManagedObjectPoolSourceContext context) throws Exception {
		// Create and populate the meta-data
		this.metaData = new MetaData(context);
		this.loadMetaData(this.metaData);

		// Return the meta-data
		return this.metaData;
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
	 * Context for the {@link ManagedObjectPoolSource#getMetaData()}.
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
		 * Specifies the {@link Thread} complete listener.
		 * 
		 * @param threadCompleteListener
		 *            {@link Thread} complete listener.
		 */
		void setThreadCompleteListener(Runnable threadCompleteListener);
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
		 * {@link Thread} complete listener.
		 */
		private Runnable threadCompleteListener;

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
		public void setThreadCompleteListener(Runnable threadCompleteListener) {
			this.threadCompleteListener = threadCompleteListener;
		}

		/*
		 * ================= ManagedObjectPoolSourceMetaData =================
		 */

		@Override
		public Class<?> getPooledObjectType() {
			return this.pooledObjectType;
		}

		@Override
		public Runnable getThreadCompleteListener() {
			return this.threadCompleteListener;
		}
	}

}