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

package net.officefloor.compile.spi.administration.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.administration.source.AdministrationEscalationMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationFlowMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationGovernanceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.AdministrationSourceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Abstract {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdministrationSource<E, F extends Enum<F>, G extends Enum<G>>
		implements AdministrationSource<E, F, G> {

	/*
	 * ==================== AdministratorSource ==============================
	 */

	@Override
	public AdministrationSourceSpecification getSpecification() {
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
	 * Context for the {@link AbstractAdministrationSource#getSpecification()}.
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
		 *            {@link AdministrationSourceProperty}.
		 */
		void addProperty(AdministrationSourceProperty property);
	}

	/**
	 * Specification for this {@link AdministrationSource}.
	 */
	private class Specification implements SpecificationContext, AdministrationSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<AdministrationSourceProperty> properties = new LinkedList<AdministrationSourceProperty>();

		/*
		 * ================= SpecificationContext =======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new AdministrationSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new AdministrationSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(AdministrationSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================== AdministratorSourceSpecification ===============
		 */

		@Override
		public AdministrationSourceProperty[] getProperties() {
			return this.properties.toArray(new AdministrationSourceProperty[0]);
		}
	}

	@Override
	public AdministrationSourceMetaData<E, F, G> init(AdministrationSourceContext context) throws Exception {

		// Create and populate the meta-data
		MetaData metaData = new MetaData(context);
		this.loadMetaData(metaData);

		// Return the meta-data
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
	protected abstract void loadMetaData(MetaDataContext<E, F, G> context) throws Exception;

	/**
	 * Context for the
	 * {@link AdministrationSource#init(AdministrationSourceContext)}.
	 */
	public static interface MetaDataContext<E, F extends Enum<F>, G extends Enum<G>> {

		/**
		 * Obtains the {@link AdministrationSourceContext}.
		 * 
		 * @return {@link AdministrationSourceContext}.
		 */
		AdministrationSourceContext getAdministrationSourceContext();

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<E> extensionInterface);

		/**
		 * Specifies the {@link AdministrationFactory}.
		 * 
		 * @param administrationFactory
		 *            {@link AdministrationFactory}.
		 */
		void setAdministrationFactory(AdministrationFactory<E, F, G> administrationFactory);

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
		 * Adds a required {@link Flow} identified by an index into the order the
		 * {@link Flow} was added.
		 * 
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller addFlow(Class<?> argumentType);

		/**
		 * Adds an {@link Escalation}.
		 * 
		 * @param escalationType
		 *            Type of {@link Escalation}.
		 * @return {@link Labeller} to possibly label the {@link Escalation}.
		 */
		Labeller addEscalation(Class<? extends Throwable> escalationType);

		/**
		 * Adds {@link Governance} identified by the key.
		 * 
		 * @param key
		 *            {@link Enum} to identify the {@link Governance}.
		 * @return {@link Labeller} to possibly label the {@link Governance}.
		 */
		Labeller addGovernance(G key);

		/**
		 * Adds {@link Governance} identified by an index into the order the
		 * {@link Governance} was added.
		 * 
		 * @return {@link Labeller} to possibly label the {@link Governance}.
		 */
		Labeller addGovernance();
	}

	/**
	 * Provides the ability to label the {@link Flow}.
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
		 * Obtains the index of the {@link Flow}.
		 * 
		 * @return Index of the {@link Flow}.
		 */
		int getIndex();
	}

	/**
	 * Meta-data of the {@link AdministrationSource}.
	 */
	private class MetaData implements MetaDataContext<E, F, G>, AdministrationSourceMetaData<E, F, G> {

		/**
		 * {@link AdministrationSourceContext}.
		 */
		private final AdministrationSourceContext context;

		/**
		 * Listing of {@link AdministrationFlowMetaData} instances.
		 */
		private final List<AdministrationFlowMetaData<?>> flows = new LinkedList<>();

		/**
		 * Listing of {@link AdministrationEscalationMetaData} instances.
		 */
		private final List<AdministrationEscalationMetaData> escalations = new LinkedList<>();

		/**
		 * Listing of {@link AdministrationGovernanceMetaData} instances.
		 */
		private final List<AdministrationGovernanceMetaData<?>> governances = new LinkedList<>();

		/**
		 * Extension interface.
		 */
		private Class<E> extensionInterface;

		/**
		 * {@link AdministrationFactory}.
		 */
		private AdministrationFactory<E, F, G> administrationFactory;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link AdministrationSourceContext}.
		 */
		private MetaData(AdministrationSourceContext context) {
			this.context = context;
		}

		/*
		 * ================= MetaDataContext ================================
		 */

		@Override
		public AdministrationSourceContext getAdministrationSourceContext() {
			return this.context;
		}

		@Override
		public void setExtensionInterface(Class<E> extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		@Override
		public void setAdministrationFactory(AdministrationFactory<E, F, G> administrationFactory) {
			this.administrationFactory = administrationFactory;
		}

		@Override
		public Labeller addFlow(F key, Class<?> argumentType) {
			AdministrationFlowMetaDataImpl<F> flow = new AdministrationFlowMetaDataImpl<F>(key, argumentType,
					key.ordinal());
			this.flows.add(flow);
			return flow;
		}

		@Override
		public Labeller addFlow(Class<?> argumentType) {
			AdministrationFlowMetaDataImpl<Indexed> flow = new AdministrationFlowMetaDataImpl<Indexed>(null,
					argumentType, this.flows.size());
			this.flows.add(flow);
			return flow;
		}

		@Override
		public Labeller addEscalation(Class<? extends Throwable> escalationType) {
			AdministrationEscalationMetaDataImpl escalation = new AdministrationEscalationMetaDataImpl(escalationType,
					this.escalations.size());
			this.escalations.add(escalation);
			return escalation;
		}

		@Override
		public Labeller addGovernance(G key) {
			AdministrationGovernanceMetaDataImpl<G> governance = new AdministrationGovernanceMetaDataImpl<G>(key,
					key.ordinal());
			this.governances.add(governance);
			return governance;
		}

		@Override
		public Labeller addGovernance() {
			AdministrationGovernanceMetaDataImpl<G> governance = new AdministrationGovernanceMetaDataImpl<G>(null,
					this.governances.size());
			this.governances.add(governance);
			return governance;
		}

		/*
		 * ================= AdministratorSourceMetaData =======================
		 */

		@Override
		public Class<E> getExtensionInterface() {
			return this.extensionInterface;
		}

		@Override
		public AdministrationFactory<E, F, G> getAdministrationFactory() {
			return this.administrationFactory;
		}

		@Override
		@SuppressWarnings("unchecked")
		public AdministrationFlowMetaData<F>[] getFlowMetaData() {
			return this.flows.toArray(new AdministrationFlowMetaData[0]);
		}

		@Override
		public AdministrationEscalationMetaData[] getEscalationMetaData() {
			return this.escalations.toArray(new AdministrationEscalationMetaData[0]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public AdministrationGovernanceMetaData<G>[] getGovernanceMetaData() {
			return this.governances.toArray(new AdministrationGovernanceMetaData[0]);
		}
	}

	/**
	 * {@link AdministrationFlowMetaData} implementation.
	 */
	private static class AdministrationFlowMetaDataImpl<F extends Enum<F>>
			implements Labeller, AdministrationFlowMetaData<F> {

		/**
		 * Key identifying this {@link Flow}.
		 */
		private final F key;

		/**
		 * Argument type to the {@link Flow}.
		 */
		private final Class<?> argumentType;

		/**
		 * Index of this {@link Flow}.
		 */
		private final int index;

		/**
		 * Label for this {@link Flow}.
		 */
		private String label;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            Key identifying this {@link Flow}.
		 * @param argumentType
		 *            Argument type to the {@link Flow}.
		 * @param index
		 *            Index of this {@link Flow}.
		 */
		private AdministrationFlowMetaDataImpl(F key, Class<?> argumentType, int index) {
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
		 * ============= AdministrationFlowMetaData ==================
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

	/**
	 * {@link AdministrationEscalationMetaData} implementation.
	 */
	private static class AdministrationEscalationMetaDataImpl implements Labeller, AdministrationEscalationMetaData {

		/**
		 * Type of {@link Escalation}.
		 */
		private final Class<? extends Throwable> escalationType;

		/**
		 * Index of this {@link Escalation}.
		 */
		private final int index;

		/**
		 * Label for this {@link Escalation}.
		 */
		private String label;

		/**
		 * Initiate.
		 * 
		 * @param escalationType
		 *            Type of {@link Escalation}.
		 * @param index
		 *            Index of this {@link Escalation}.
		 */
		private AdministrationEscalationMetaDataImpl(Class<? extends Throwable> escalationType, int index) {
			this.escalationType = escalationType;
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
		 * ============= AdministrationEscalationMetaData ===============
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> Class<E> getEscalationType() {
			return (Class<E>) this.escalationType;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
	}

	/**
	 * {@link AdministrationGovernanceMetaData} implementation.
	 */
	private static class AdministrationGovernanceMetaDataImpl<G extends Enum<G>>
			implements Labeller, AdministrationGovernanceMetaData<G> {

		/**
		 * Key identifying this {@link Governance}.
		 */
		private final G key;

		/**
		 * Index of this {@link Escalation}.
		 */
		private final int index;

		/**
		 * Label for this {@link Escalation}.
		 */
		private String label;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            Key identifying this {@link Escalation}.
		 * @param index
		 *            Index of this {@link Escalation}.
		 */
		private AdministrationGovernanceMetaDataImpl(G key, int index) {
			this.key = key;
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
		 * ========== AdministrationGovernanceMetaData ===============
		 */

		@Override
		public G getKey() {
			return this.key;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
	}

}
