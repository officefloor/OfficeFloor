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
package net.officefloor.frame.spi.administration.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;

/**
 * Abstract {@link AdministratorSource}.
 * 
 * @author Daniel
 */
public abstract class AbstractAdministratorSource<I, A extends Enum<A>>
		implements AdministratorSource<I, A> {

	/*
	 * ==================== AdministratorSource ==============================
	 */

	@Override
	public AdministratorSourceSpecification getSpecification() {
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
	 * Context for the {@link AbstractAdministratorSource#getSpecification()}.
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
		void addProperty(AdministratorSourceProperty property);
	}

	/**
	 * Specification for this {@link AdministratorSource}.
	 */
	private class Specification implements SpecificationContext,
			AdministratorSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<AdministratorSourceProperty> properties = new LinkedList<AdministratorSourceProperty>();

		/*
		 * ================= SpecificationContext =======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties
					.add(new AdministratorSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties
					.add(new AdministratorSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(AdministratorSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================== AdministratorSourceSpecification ===============
		 */

		@Override
		public AdministratorSourceProperty[] getProperties() {
			return this.properties.toArray(new AdministratorSourceProperty[0]);
		}
	}

	/**
	 * {@link MetaData}.
	 */
	private MetaData metaData;

	@Override
	public void init(AdministratorSourceContext context) throws Exception {
		// Create and populate the meta-data
		this.metaData = new MetaData(context);
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
	protected abstract void loadMetaData(MetaDataContext<I, A> context)
			throws Exception;

	/**
	 * Context for the {@link AdministratorSource#getMetaData()}.
	 */
	public static interface MetaDataContext<I, A> {

		/**
		 * Obtains the {@link AdministratorSourceContext}.
		 * 
		 * @return {@link AdministratorSourceContext}.
		 */
		AdministratorSourceContext getAdministratorSourceContext();

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<I> extensionInterface);

		/**
		 * Adds meta-data for a {@link Duty} that required no {@link Flow}
		 * instances.
		 * 
		 * @param dutyKey
		 *            Key identifying the {@link Duty}.
		 */
		void addDuty(A dutyKey);

		/**
		 * Adds meta-data for a {@link Duty} that requires {@link Flow}
		 * instances.
		 * 
		 * @param dutyKey
		 *            Key identifying the {@link Duty}.
		 * @param flowKeys
		 *            {@link Enum} {@link Class} specifying the {@link Duty}
		 *            flow keys.
		 */
		<F extends Enum<F>> void addDuty(A dutyKey, Class<F> flowKeys);
	}

	/**
	 * Meta-data of the {@link AdministratorSource}.
	 */
	private class MetaData implements MetaDataContext<I, A>,
			AdministratorSourceMetaData<I, A> {

		/**
		 * {@link AdministratorSourceContext}.
		 */
		private final AdministratorSourceContext context;

		/**
		 * {@link DutyMetaData} for the {@link Duty} key.
		 */
		private final List<DutyMetaData<?>> dutyMetaData = new LinkedList<DutyMetaData<?>>();

		/**
		 * Extension interface.
		 */
		private Class<I> extensionInterface;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link AdministratorSourceContext}.
		 */
		public MetaData(AdministratorSourceContext context) {
			this.context = context;
		}

		/*
		 * ================= MetaDataContext ================================
		 */

		@Override
		public AdministratorSourceContext getAdministratorSourceContext() {
			return this.context;
		}

		@Override
		public void setExtensionInterface(Class<I> extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		@Override
		public void addDuty(A dutyKey) {
			this.dutyMetaData.add(new DutyMetaData<None>(dutyKey, None.class));
		}

		@Override
		public <F extends Enum<F>> void addDuty(A dutyKey, Class<F> flowKeys) {
			this.dutyMetaData.add(new DutyMetaData<F>(dutyKey, flowKeys));
		}

		/*
		 * ================= AdministratorSourceMetaData =======================
		 */

		@Override
		public Class<I> getExtensionInterface() {
			return this.extensionInterface;
		}

		@Override
		@SuppressWarnings("unchecked")
		public AdministratorDutyMetaData<A, ?>[] getAdministratorDutyMetaData() {
			return this.dutyMetaData.toArray(new AdministratorDutyMetaData[0]);
		}
	}

	/**
	 * {@link AdministratorDutyMetaData} implementation.
	 */
	private class DutyMetaData<F extends Enum<F>> implements
			AdministratorDutyMetaData<A, F> {

		/**
		 * Key to the {@link Duty}.
		 */
		private final A dutyKey;

		/**
		 * Flow keys for the {@link Duty}.
		 */
		private final Class<F> flowKeys;

		/**
		 * Initiate.
		 * 
		 * @param dutyKey
		 *            Key to the {@link Duty}.
		 * @param flowKeys
		 *            Flow keys for the {@link Duty}.
		 */
		public DutyMetaData(A dutyKey, Class<F> flowKeys) {
			this.dutyKey = dutyKey;
			this.flowKeys = flowKeys;
		}

		/*
		 * ================= AdministratorDutyMetaData ======================
		 */

		@Override
		public A getKey() {
			return this.dutyKey;
		}

		@Override
		public Class<F> getFlowKeys() {
			return this.flowKeys;
		}
	}

	@Override
	public AdministratorSourceMetaData<I, A> getMetaData() {
		// Return the meta data
		return this.metaData;
	}

	/**
	 * {@link AdministratorSource#createAdministrator()} to be overridden.
	 */

}