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
package net.officefloor.frame.spi.administration.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyFlowMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * Abstract {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
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
		 *            {@link AdministratorSourceProperty}.
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
		 * Adds meta-data for a {@link Duty} identifying by the key. The name of
		 * the duty will be taken from the key.
		 * 
		 * @param dutyKey
		 *            Key identifying the {@link Duty}.
		 * @return {@link DutyMetaDataContext} to provide meta-data for the
		 *         {@link Duty}.
		 */
		DutyMetaDataContext addDuty(A dutyKey);

		/**
		 * Adds meta-data for a {@link Duty} identifying by the index into the
		 * order the {@link Duty} instances were added.
		 * 
		 * @param dutyName
		 *            Name identifying the {@link Duty}.
		 * @return {@link DutyMetaDataContext} to provide meta-data for the
		 *         {@link Duty}.
		 */
		DutyMetaDataContext addDuty(String dutyName);
	}

	/**
	 * Provides the ability to label the {@link Duty} or {@link JobSequence}.
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
		 * Obtains the index of the {@link Duty} or {@link JobSequence}.
		 * 
		 * @return Index of the {@link Duty} or {@link JobSequence}.
		 */
		int getIndex();
	}

	/**
	 * Context for the {@link AdministratorDutyMetaData}.
	 */
	public static interface DutyMetaDataContext {

		/**
		 * Adds a required {@link JobSequence} identified by the key.
		 *
		 * @param <F>
		 *            Flow key type.
		 * @param key
		 *            {@link Enum} to identify the {@link JobSequence}.
		 * @param argumentType
		 *            Type of argument passed to the {@link JobSequence}.
		 * @return {@link Labeller} to possibly label the {@link JobSequence}.
		 */
		<F extends Enum<F>> Labeller addFlow(F key, Class<?> argumentType);

		/**
		 * Adds a required {@link JobSequence} identified by an index into the
		 * order the {@link JobSequence} was added.
		 * 
		 * @param argumentType
		 *            Type of argument passed to the {@link JobSequence}.
		 * @return {@link Labeller} to possibly label the {@link JobSequence}.
		 */
		Labeller addFlow(Class<?> argumentType);
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
		public DutyMetaDataContext addDuty(A dutyKey) {
			DutyMetaData<Indexed> duty = new DutyMetaData<Indexed>(dutyKey);
			this.dutyMetaData.add(duty);
			return duty;
		}

		@Override
		public DutyMetaDataContext addDuty(String dutyName) {
			DutyMetaData<Indexed> duty = new DutyMetaData<Indexed>(dutyName);
			this.dutyMetaData.add(duty);
			return duty;
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
			DutyMetaDataContext, AdministratorDutyMetaData<A, F> {

		/**
		 * Name of the {@link Duty}.
		 */
		private final String dutyName;

		/**
		 * Key to the {@link Duty}.
		 */
		private final A dutyKey;

		/**
		 * Listing of {@link AdministratorDutyFlowMetaData} instances.
		 */
		private final List<AdministratorDutyFlowMetaData<?>> flows = new LinkedList<AdministratorDutyFlowMetaData<?>>();

		/**
		 * Initiate.
		 * 
		 * @param dutyName
		 *            Name for the {@link Duty}.
		 */
		public DutyMetaData(String dutyName) {
			this.dutyName = dutyName;
			this.dutyKey = null;
		}

		/**
		 * Initiate.
		 * 
		 * @param dutyKey
		 *            Key to the {@link Duty}.
		 */
		public DutyMetaData(A dutyKey) {
			this.dutyName = dutyKey.name();
			this.dutyKey = dutyKey;
		}

		/*
		 * ================= AdministratorDutyMetaData ======================
		 */

		@Override
		public String getDutyName() {
			return this.dutyName;
		}

		@Override
		public A getKey() {
			return this.dutyKey;
		}

		@Override
		@SuppressWarnings("unchecked")
		public AdministratorDutyFlowMetaData<F>[] getFlowMetaData() {
			return this.flows.toArray(new AdministratorDutyFlowMetaData[0]);
		}

		/*
		 * ================ DutyMetaDataContext =========================
		 */

		@Override
		public <f extends Enum<f>> Labeller addFlow(f key, Class<?> argumentType) {
			DutyFlowMetaData<f> flow = new DutyFlowMetaData<f>(key,
					argumentType, key.ordinal());
			this.flows.add(flow);
			return flow;
		}

		@Override
		public Labeller addFlow(Class<?> argumentType) {
			DutyFlowMetaData<Indexed> flow = new DutyFlowMetaData<Indexed>(
					null, argumentType, this.flows.size());
			this.flows.add(flow);
			return flow;
		}
	}

	/**
	 * {@link AdministratorDutyFlowMetaData} implementation.
	 */
	private static class DutyFlowMetaData<F extends Enum<F>> implements
			Labeller, AdministratorDutyFlowMetaData<F> {

		/**
		 * Key identifying this {@link JobSequence}.
		 */
		private final F key;

		/**
		 * Argument type to the {@link JobSequence}.
		 */
		private final Class<?> argumentType;

		/**
		 * Index of this {@link JobSequence}.
		 */
		private final int index;

		/**
		 * Label for this {@link JobSequence}.
		 */
		private String label;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            Key identifying this {@link JobSequence}.
		 * @param argumentType
		 *            Argument type to the {@link JobSequence}.
		 * @param index
		 *            Index of this {@link JobSequence}.
		 */
		public DutyFlowMetaData(F key, Class<?> argumentType, int index) {
			this.key = key;
			this.argumentType = argumentType;
			this.index = index;
		}

		/*
		 * ==================== Labeller ===============================
		 */

		@Override
		public Labeller setLabel(String label) {
			this.label = label;
			return this;
		}

		@Override
		public int getIndex() {
			return this.index;
		}

		/*
		 * ============= AdministratorDutyFlowMetaData ==================
		 */

		@Override
		public F getKey() {
			return this.key;
		}

		@Override
		public Class<?> getArgumentType() {
			return this.argumentType;
		}

		@Override
		public String getLabel() {
			return this.label;
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