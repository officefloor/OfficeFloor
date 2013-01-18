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
package net.officefloor.plugin.web.http.security.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.plugin.web.http.security.HttpSecurityDependencyMetaData;
import net.officefloor.plugin.web.http.security.HttpSecurityFlowMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceProperty;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;

/**
 * Abstract {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecuritySource<S, C, D extends Enum<D>, F extends Enum<F>>
		implements HttpSecuritySource<S, C, D, F> {

	/*
	 * ====================== HttpSecuritySource =============================
	 */

	@Override
	public HttpSecuritySourceSpecification getSpecification() {
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
	 * Context for the {@link HttpSecurityObjectSource#getSpecification()}.
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
		 *            {@link HttpSecuritySourceProperty}.
		 */
		void addProperty(HttpSecuritySourceProperty property);
	}

	/**
	 * Specification for this {@link HttpSecuritySource}.
	 */
	private class Specification implements SpecificationContext,
			HttpSecuritySourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<HttpSecuritySourceProperty> properties = new LinkedList<HttpSecuritySourceProperty>();

		/*
		 * ========== SpecificationContext ========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new HttpSecuritySourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties
					.add(new HttpSecuritySourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(HttpSecuritySourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ========== HttpSecuritySourceSpecification ===========
		 */

		@Override
		public HttpSecuritySourceProperty[] getProperties() {
			return this.properties.toArray(new HttpSecuritySourceProperty[0]);
		}
	}

	/**
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private MetaData metaData = null;

	@Override
	public void init(HttpSecuritySourceContext context) throws Exception {

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
	protected abstract void loadMetaData(MetaDataContext<S, C, D, F> context)
			throws Exception;

	/**
	 * Context for the {@link HttpSecuritySource#getMetaData()}.
	 */
	public static interface MetaDataContext<S, C, D extends Enum<D>, F extends Enum<F>> {

		/**
		 * Obtains the {@link HttpSecuritySourceContext}.
		 * 
		 * @return {@link HttpSecuritySourceContext}.
		 */
		HttpSecuritySourceContext getHttpSecuritySourceContext();

		/**
		 * Specifies the type for security.
		 * 
		 * @param securityClass
		 *            Security type.
		 */
		void setSecurityClass(Class<S> securityClass);

		/**
		 * Specifies the type for credentials.
		 * 
		 * @param credentialsClass
		 *            Credentials type.
		 */
		void setCredentialsClass(Class<C> credentialsClass);

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
		DependencyLabeller addDependency(D key, Class<?> dependencyType);

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
		 * Adds a required {@link JobSequence} identified by the key.
		 * 
		 * @param key
		 *            {@link Enum} to identify the {@link JobSequence}.
		 * @param argumentType
		 *            Type of argument passed to the {@link JobSequence}.
		 * @return {@link Labeller} to possibly label the {@link JobSequence}.
		 */
		Labeller addFlow(F key, Class<?> argumentType);

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
	 * Provides the ability to label the required dependency or
	 * {@link JobSequence}.
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
		 * Obtains the index of the dependency of {@link JobSequence}.
		 * 
		 * @return Index of the dependency of {@link JobSequence}.
		 */
		int getIndex();
	}

	/**
	 * Meta-data for the {@link HttpSecuritySource}.
	 */
	private class MetaData implements MetaDataContext<S, C, D, F>,
			HttpSecuritySourceMetaData<S, C, D, F> {

		/**
		 * {@link HttpSecuritySourceContext}.
		 */
		private final HttpSecuritySourceContext context;

		/**
		 * Security {@link Class}.
		 */
		private Class<S> securityClass;

		/**
		 * Credentials {@link Class}.
		 */
		private Class<C> credentialsClass = null;

		/**
		 * {@link HttpSecurityDependencyMetaData} instances.
		 */
		private Map<Integer, HttpSecurityDependencyMetaData<D>> dependencies = new HashMap<Integer, HttpSecurityDependencyMetaData<D>>();

		/**
		 * {@link HttpSecurityFlowMetaData} instances.
		 */
		private Map<Integer, HttpSecurityFlowMetaData<F>> flows = new HashMap<Integer, HttpSecurityFlowMetaData<F>>();

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link HttpSecuritySourceContext}.
		 */
		public MetaData(HttpSecuritySourceContext context) {
			this.context = context;
		}

		/*
		 * ============ MetaDataContext ==========================
		 */

		@Override
		public HttpSecuritySourceContext getHttpSecuritySourceContext() {
			return this.context;
		}

		@Override
		public void setSecurityClass(Class<S> securityClass) {
			this.securityClass = securityClass;
		}

		@Override
		public void setCredentialsClass(Class<C> credentialsClass) {
			this.credentialsClass = credentialsClass;
		}

		@Override
		public DependencyLabeller addDependency(D key, Class<?> dependencyType) {
			// Use ordinal of key to index the dependency
			return this.addDependency(key.ordinal(), key, dependencyType);
		}

		@Override
		public DependencyLabeller addDependency(Class<?> dependencyType) {
			// Indexed, so use next index (size will increase with indexing)
			return this.addDependency(this.dependencies.size(), null,
					dependencyType);
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
		private DependencyLabeller addDependency(final int index, D key,
				Class<?> dependencyType) {

			// Create the dependency meta-data
			final HttpSecurityDependencyMetaDataImpl<D> dependency = new HttpSecurityDependencyMetaDataImpl<D>(
					key, dependencyType);

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
		 * Adds a {@link JobSequence}.
		 * 
		 * @param index
		 *            Index to add the {@link JobSequence} under.
		 * @param key
		 *            Key for the {@link JobSequence}. May be <code>null</code>.
		 * @param argumentType
		 *            Type of the argument passed to the {@link JobSequence}.
		 * @return {@link Labeller} for the {@link JobSequence}.
		 */
		private Labeller addFlow(final int index, F key, Class<?> argumentType) {

			// Create the flow meta-data
			final HttpSecurityFlowMetaDataImpl<F> flow = new HttpSecurityFlowMetaDataImpl<F>(
					key, argumentType);

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

		/*
		 * ============== HttpSecuritySourceMetaData ===============
		 */

		@Override
		public Class<S> getSecurityClass() {
			return this.securityClass;
		}

		@Override
		public Class<C> getCredentialsClass() {
			return this.credentialsClass;
		}

		@Override
		public HttpSecurityDependencyMetaData<D>[] getDependencyMetaData() {
			return ConstructUtil.toArray(this.dependencies,
					new HttpSecurityDependencyMetaData[0]);
		}

		@Override
		public HttpSecurityFlowMetaData<F>[] getFlowMetaData() {
			return ConstructUtil.toArray(this.flows,
					new HttpSecurityFlowMetaData[0]);
		}
	}

	@Override
	public HttpSecuritySourceMetaData<S, C, D, F> getMetaData() {
		return this.metaData;
	}

}