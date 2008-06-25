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

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.None;
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
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#getSpecification()
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

		// ---------------------------------------------------------------
		// SpecificationContext
		// ---------------------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.SpecificationContext#addProperty(java.lang.String)
		 */
		@Override
		public void addProperty(String name) {
			this.properties
					.add(new AdministratorSourcePropertyImpl(name, name));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.SpecificationContext#addProperty(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void addProperty(String name, String label) {
			this.properties
					.add(new AdministratorSourcePropertyImpl(name, label));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.SpecificationContext#addProperty(net.officefloor.frame.spi.administration.source.AdministratorSourceProperty)
		 */
		@Override
		public void addProperty(AdministratorSourceProperty property) {
			this.properties.add(property);
		}

		// ---------------------------------------------------------------
		// AdministratorSourceSpecification
		// ---------------------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification#getProperties()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#init(net.officefloor.frame.spi.administration.source.AdministratorSourceContext)
	 */
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
		 * Specifies the {@link Enum} of the {@link Duty} keys.
		 * 
		 * @param dutyKeys
		 *            {@link Enum} listing {@link Duty} keys.
		 */
		void setDutyKeys(Class<A> dutyKeys);

		/**
		 * <p>
		 * Specifies the {@link Enum} of the flows for the {@link Duty}.
		 * <p>
		 * The default flow keys is {@link None} so this method need only be
		 * called should the {@link Duty} require flows.
		 * 
		 * @param dutyKey
		 *            {@link Duty} key {@link Enum}.
		 * @param flowKeys
		 *            {@link Enum} {@link Class} specifying the {@link Duty}
		 *            flow keys.
		 */
		<F extends Enum<F>> void setDutyFlows(A dutyKey, Class<F> flowKeys);

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<I> extensionInterface);
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
		 * {@link Enum} listing {@link Duty} keys.
		 */
		private Class<A> dutyKeys;

		/**
		 * {@link DutyMetaData} for the {@link Duty} key.
		 */
		private Map<A, DutyMetaData<?>> dutyMetaData;

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

		// --------------------------------------------------------------------
		// MetaDataContext
		// --------------------------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.MetaDataContext#setDutyKeys(java.lang.Class)
		 */
		@Override
		public void setDutyKeys(Class<A> dutyKeys) {

			// Specify the duty keys
			this.dutyKeys = dutyKeys;

			// Create the duty meta-data, with each having no flows
			this.dutyMetaData = new EnumMap<A, DutyMetaData<?>>(this.dutyKeys);
			for (A dutyKey : this.dutyKeys.getEnumConstants()) {
				this.dutyMetaData.put(dutyKey, new DutyMetaData<None>(
						None.class));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.MetaDataContext#setDutyFlows(java.lang.Object,
		 *      java.lang.Class)
		 */
		@Override
		public <F extends Enum<F>> void setDutyFlows(A dutyKey,
				Class<F> flowKeys) {
			this.dutyMetaData.put(dutyKey, new DutyMetaData<F>(flowKeys));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.MetaDataContext#setExtensionInterface(java.lang.Class)
		 */
		@Override
		public void setExtensionInterface(Class<I> extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		// --------------------------------------------------------------------
		// AdministratorSourceMetaData
		// --------------------------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.MetaDataContext#getAdministratorSourceContext()
		 */
		@Override
		public AdministratorSourceContext getAdministratorSourceContext() {
			return this.context;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData#getAministratorDutyKeys()
		 */
		@Override
		public Class<A> getAministratorDutyKeys() {
			return this.dutyKeys;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData#getAdministratorDutyMetaData(java.lang.Enum)
		 */
		@Override
		public AdministratorDutyMetaData<?> getAdministratorDutyMetaData(A key) {
			return this.dutyMetaData.get(key);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData#getExtensionInterface()
		 */
		@Override
		public Class<I> getExtensionInterface() {
			return this.extensionInterface;
		}
	}

	/**
	 * {@link AdministratorDutyMetaData} implementation.
	 */
	private class DutyMetaData<F extends Enum<F>> implements
			AdministratorDutyMetaData<F> {

		/**
		 * Flow keys for the {@link Duty}.
		 */
		private final Class<F> flowKeys;

		/**
		 * Initiate.
		 * 
		 * @param flowKeys
		 *            Flow keys for the {@link Duty}.
		 */
		public DutyMetaData(Class<F> flowKeys) {
			this.flowKeys = flowKeys;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData#getFlowKeys()
		 */
		@Override
		public Class<F> getFlowKeys() {
			return this.flowKeys;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#getMetaData()
	 */
	@Override
	public AdministratorSourceMetaData<I, A> getMetaData() {
		// Return the meta data
		return this.metaData;
	}

	/**
	 * {@link AdministratorSource#createAdministrator()} to be overridden.
	 */

}
