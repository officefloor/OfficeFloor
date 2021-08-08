/*-
 * #%L
 * Web Security
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

package net.officefloor.web.spi.security.impl;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;
import net.officefloor.web.spi.security.HttpSecurityDependencyMetaData;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecurityFlowMetaData;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySourceProperty;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;

/**
 * Abstract {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecuritySource<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
		implements HttpSecuritySource<A, AC, C, O, F> {

	/**
	 * UTF-8 {@link Charset}.
	 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

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
	 * @param context Specifications.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for the {@link HttpSecuritySource#getSpecification()}.
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
		 * @param property {@link HttpSecuritySourceProperty}.
		 */
		void addProperty(HttpSecuritySourceProperty property);
	}

	/**
	 * Specification for this {@link HttpSecuritySource}.
	 */
	private class Specification implements SpecificationContext, HttpSecuritySourceSpecification {

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
			this.properties.add(new HttpSecuritySourcePropertyImpl(name, label));
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

	@Override
	public HttpSecuritySourceMetaData<A, AC, C, O, F> init(HttpSecuritySourceContext context) throws Exception {

		// Create the meta-data
		MetaData metaData = new MetaData(context);
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
	protected abstract void loadMetaData(MetaDataContext<A, AC, C, O, F> context) throws Exception;

	/**
	 * Context for the {@link HttpSecuritySource#init(HttpSecuritySourceContext)}.
	 */
	public static interface MetaDataContext<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

		/**
		 * Obtains the {@link HttpSecuritySourceContext}.
		 * 
		 * @return {@link HttpSecuritySourceContext}.
		 */
		HttpSecuritySourceContext getHttpSecuritySourceContext();

		/**
		 * Specifies the type for authentication.
		 * 
		 * @param authenticationClass Authentication type.
		 */
		void setAuthenticationClass(Class<A> authenticationClass);

		/**
		 * Specifies the {@link HttpAuthenticationFactory}.
		 * 
		 * @param httpAuthenticationFactory {@link HttpAuthenticationFactory}.
		 */
		void setHttpAuthenticationFactory(HttpAuthenticationFactory<A, C> httpAuthenticationFactory);

		/**
		 * Specifies the type for access control.
		 * 
		 * @param accessControlClass Access control type.
		 */
		void setAccessControlClass(Class<AC> accessControlClass);

		/**
		 * Specifies the {@link HttpAccessControlFactory}.
		 * 
		 * @param httpAccessControlFactory {@link HttpAccessControlFactory}.
		 */
		void setHttpAccessControlFactory(HttpAccessControlFactory<AC> httpAccessControlFactory);

		/**
		 * Specifies the type for credentials.
		 * 
		 * @param credentialsClass Credentials type.
		 */
		void setCredentialsClass(Class<C> credentialsClass);

		/**
		 * Adds a required dependency identified by the key.
		 * 
		 * @param key            {@link Enum} to identify the dependency.
		 * @param dependencyType Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required dependency.
		 */
		DependencyLabeller addDependency(O key, Class<?> dependencyType);

		/**
		 * Adds a required dependency identified by an index into the order the
		 * dependency was added.
		 * 
		 * @param dependencyType Type the dependency is required to extend/implement.
		 * @return {@link DependencyLabeller} to possibly label the required dependency.
		 */
		DependencyLabeller addDependency(Class<?> dependencyType);

		/**
		 * Adds a required {@link Flow} identified by the key.
		 * 
		 * @param key          {@link Enum} to identify the {@link Flow}.
		 * @param argumentType Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
		 */
		Labeller addFlow(F key, Class<?> argumentType);

		/**
		 * Adds a required {@link Flow} identified by an index into the order the
		 * {@link Flow} was added.
		 * 
		 * @param argumentType Type of argument passed to the {@link Flow}.
		 * @return {@link Labeller} to possibly label the {@link Flow}.
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
		 * @param qualifier Type qualifier.
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
		 * @param label Label.
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
	 * Meta-data for the {@link HttpSecuritySource}.
	 */
	private class MetaData implements MetaDataContext<A, AC, C, O, F>, HttpSecuritySourceMetaData<A, AC, C, O, F> {

		/**
		 * {@link HttpSecuritySourceContext}.
		 */
		private final HttpSecuritySourceContext context;

		/**
		 * Authentication {@link Class}.
		 */
		private Class<A> authenticationClass;

		/**
		 * {@link HttpAuthenticationFactory}.
		 */
		private HttpAuthenticationFactory<A, C> httpAuthenticationFactory;

		/**
		 * Access control {@link Class}.
		 */
		private Class<AC> accessControlClass;

		/**
		 * {@link HttpAccessControlFactory}.
		 */
		private HttpAccessControlFactory<AC> httpAccessControlFactory;

		/**
		 * Credentials {@link Class}.
		 */
		private Class<C> credentialsClass = null;

		/**
		 * {@link HttpSecurityDependencyMetaData} instances.
		 */
		private Map<Integer, HttpSecurityDependencyMetaData<O>> dependencies = new HashMap<>();

		/**
		 * {@link HttpSecurityFlowMetaData} instances.
		 */
		private Map<Integer, HttpSecurityFlowMetaData<F>> flows = new HashMap<>();

		/**
		 * Initiate.
		 * 
		 * @param context {@link HttpSecuritySourceContext}.
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
		public void setAuthenticationClass(Class<A> authenticationClass) {
			this.authenticationClass = authenticationClass;
		}

		@Override
		public void setHttpAuthenticationFactory(HttpAuthenticationFactory<A, C> httpAuthenticationFactory) {
			this.httpAuthenticationFactory = httpAuthenticationFactory;
		}

		@Override
		public void setAccessControlClass(Class<AC> accessControlClass) {
			this.accessControlClass = accessControlClass;
		}

		@Override
		public void setHttpAccessControlFactory(HttpAccessControlFactory<AC> httpAccessControlFactory) {
			this.httpAccessControlFactory = httpAccessControlFactory;
		}

		@Override
		public void setCredentialsClass(Class<C> credentialsClass) {
			this.credentialsClass = credentialsClass;
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
		 * @param index          Index to add the dependency under.
		 * @param key            Key for the dependency. May be <code>null</code>.
		 * @param dependencyType Type of dependency.
		 * @return {@link Labeller} for the dependency.
		 */
		private DependencyLabeller addDependency(final int index, O key, Class<?> dependencyType) {

			// Create the dependency meta-data
			final HttpSecurityDependencyMetaDataImpl<O> dependency = new HttpSecurityDependencyMetaDataImpl<>(key,
					dependencyType);

			// Register the dependency at the index
			this.dependencies.put(Integer.valueOf(index), dependency);

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
		 * @param index        Index to add the {@link Flow} under.
		 * @param key          Key for the {@link Flow}. May be <code>null</code>.
		 * @param argumentType Type of the argument passed to the {@link Flow}.
		 * @return {@link Labeller} for the {@link Flow}.
		 */
		private Labeller addFlow(final int index, F key, Class<?> argumentType) {

			// Create the flow meta-data
			final HttpSecurityFlowMetaDataImpl<F> flow = new HttpSecurityFlowMetaDataImpl<F>(key, argumentType);

			// Register the flow at the index
			this.flows.put(Integer.valueOf(index), flow);

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
		public Class<A> getAuthenticationType() {
			return this.authenticationClass;
		}

		@Override
		public HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory() {
			return this.httpAuthenticationFactory;
		}

		@Override
		public Class<AC> getAccessControlType() {
			return this.accessControlClass;
		}

		@Override
		public HttpAccessControlFactory<AC> getHttpAccessControlFactory() {
			return this.httpAccessControlFactory;
		}

		@Override
		public Class<C> getCredentialsType() {
			return this.credentialsClass;
		}

		@Override
		public HttpSecurityDependencyMetaData<O>[] getDependencyMetaData() {
			return ConstructUtil.toArray(this.dependencies, new HttpSecurityDependencyMetaData[0]);
		}

		@Override
		public HttpSecurityFlowMetaData<F>[] getFlowMetaData() {
			return ConstructUtil.toArray(this.flows, new HttpSecurityFlowMetaData[0]);
		}
	}

	@Override
	public void start(HttpSecurityExecuteContext<F> context) throws Exception {
		// Override to provide start functionality
	}

	@Override
	public void stop() {
		// Override to provide start functionality
	}

}
