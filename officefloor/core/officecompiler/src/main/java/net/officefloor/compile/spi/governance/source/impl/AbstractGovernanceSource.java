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

package net.officefloor.compile.spi.governance.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.governance.source.GovernanceFlowMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Abstract {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGovernanceSource<I, F extends Enum<F>> implements GovernanceSource<I, F> {

	/*
	 * ======================= GovernanceSource =========================
	 */

	@Override
	public GovernanceSourceSpecification getSpecification() {
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
	 * Context for the {@link AbstractGovernanceSource#getSpecification()}.
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
		 *            {@link GovernanceSourceProperty}.
		 */
		void addProperty(GovernanceSourceProperty property);
	}

	/**
	 * Specification for this {@link GovernanceSource}.
	 */
	private class Specification implements SpecificationContext, GovernanceSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<GovernanceSourceProperty> properties = new LinkedList<GovernanceSourceProperty>();

		/*
		 * ================= SpecificationContext =======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new GovernanceSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new GovernanceSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(GovernanceSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================== GovernanceSourceSpecification ===============
		 */

		@Override
		public GovernanceSourceProperty[] getProperties() {
			return this.properties.toArray(new GovernanceSourceProperty[0]);
		}
	}

	@Override
	public GovernanceSourceMetaData<I, F> init(GovernanceSourceContext context) throws Exception {

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
	protected abstract void loadMetaData(MetaDataContext<I, F> context) throws Exception;

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
	 * Context for the {@link GovernanceSource#init(GovernanceSourceContext)}.
	 */
	public static interface MetaDataContext<I, F extends Enum<F>> {

		/**
		 * Obtains the {@link GovernanceSourceContext}.
		 * 
		 * @return {@link GovernanceSourceContext}.
		 */
		GovernanceSourceContext getGovernanceSourceContext();

		/**
		 * Specifies the {@link GovernanceFactory}.
		 * 
		 * @param governanceFactory
		 *            {@link GovernanceFactory}.
		 */
		void setGovernanceFactory(GovernanceFactory<? extends I, F> governanceFactory);

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<I> extensionInterface);

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
		 * Adds a potential escalation from a {@link GovernanceActivity}.
		 * 
		 * @param escalationType
		 *            Type of escalation.
		 */
		void addEscalation(Class<?> escalationType);
	}

	/**
	 * Meta-data of the {@link GovernanceSource}.
	 */
	private class MetaData implements MetaDataContext<I, F>, GovernanceSourceMetaData<I, F> {

		/**
		 * {@link GovernanceSourceContext}.
		 */
		private final GovernanceSourceContext context;

		/**
		 * {@link GovernanceFactory}.
		 */
		private GovernanceFactory<? extends I, F> governanceFactory;

		/**
		 * Extension interface.
		 */
		private Class<I> extensionInterface;

		/**
		 * Listing of {@link GovernanceFlowMetaData} instances.
		 */
		private final List<GovernanceFlowMetaData<?>> flows = new LinkedList<GovernanceFlowMetaData<?>>();

		/**
		 * Listing of escalation types.
		 */
		private final List<Class<?>> escalationTypes = new LinkedList<Class<?>>();

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link GovernanceSourceContext}.
		 */
		public MetaData(GovernanceSourceContext context) {
			this.context = context;
		}

		/*
		 * ================= MetaDataContext ================================
		 */

		@Override
		public GovernanceSourceContext getGovernanceSourceContext() {
			return this.context;
		}

		@Override
		public void setGovernanceFactory(GovernanceFactory<? extends I, F> governanceFactory) {
			this.governanceFactory = governanceFactory;
		}

		@Override
		public void setExtensionInterface(Class<I> extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		@Override
		public Labeller addFlow(F key, Class<?> argumentType) {
			FlowMetaData<F> flow = new FlowMetaData<F>(key, argumentType, key.ordinal());
			this.flows.add(flow);
			return flow;
		}

		@Override
		public Labeller addFlow(Class<?> argumentType) {
			FlowMetaData<Indexed> flow = new FlowMetaData<Indexed>(null, argumentType, this.flows.size());
			this.flows.add(flow);
			return flow;
		}

		@Override
		public void addEscalation(Class<?> escalationType) {
			this.escalationTypes.add(escalationType);
		}

		/*
		 * ================= GovernanceSourceMetaData =======================
		 */

		@Override
		public GovernanceFactory<? extends I, F> getGovernanceFactory() {
			return this.governanceFactory;
		}

		@Override
		public Class<I> getExtensionInterface() {
			return this.extensionInterface;
		}

		@Override
		@SuppressWarnings("unchecked")
		public GovernanceFlowMetaData<F>[] getFlowMetaData() {
			return this.flows.toArray(new GovernanceFlowMetaData[this.flows.size()]);
		}

		@Override
		public Class<?>[] getEscalationTypes() {
			return this.escalationTypes.toArray(new Class<?>[this.escalationTypes.size()]);
		}
	}

	/**
	 * {@link GovernanceFlowMetaData} implementation.
	 */
	private static class FlowMetaData<F extends Enum<F>> implements Labeller, GovernanceFlowMetaData<F> {

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
		public FlowMetaData(F key, Class<?> argumentType, int index) {
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
		 * ================= GovernanceFlowMetaData =====================
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

}
