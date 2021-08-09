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

package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.impl.construct.administration.AdministrationBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.thread.ThreadLocalImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ThreadLocalConfiguration;

/**
 * {@link DependencyMappingBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyMappingBuilderImpl<O extends Enum<O>> implements DependencyMappingBuilder,
		ThreadDependencyMappingBuilder, ManagedObjectConfiguration<O>, InputManagedObjectConfiguration<O> {

	/**
	 * Name of the {@link ManagedObject} is being bound.
	 */
	private final String boundManagedObjectName;

	/**
	 * Name of the {@link ManagedObject} within the {@link Office}.
	 */
	private final String officeManagedObjectName;

	/**
	 * {@link ManagedObjectDependencyConfiguration} by index of dependency.
	 */
	private final Map<Integer, ManagedObjectDependencyConfiguration<O>> dependencies = new HashMap<>();

	/**
	 * Pre-load {@link AdministrationConfiguration} instances.
	 */
	private final List<AdministrationConfiguration<?, ?, ?>> preLoadAdministrations = new LinkedList<>();

	/**
	 * {@link ManagedObjectGovernanceConfiguration} instances.
	 */
	private final List<ManagedObjectGovernanceConfiguration> governances = new LinkedList<>();

	/**
	 * {@link ThreadLocalImpl} for the {@link OptionalThreadLocal}.
	 */
	private ThreadLocalImpl<?> threadLocal = null;

	/**
	 * Initiate as a {@link ManagedObjectConfiguration}.
	 * 
	 * @param boundManagedObjectName  Name of the {@link ManagedObject} is being
	 *                                bound.
	 * @param officeManagedObjectName Name of the {@link ManagedObject} within the
	 *                                {@link Office}.
	 */
	public DependencyMappingBuilderImpl(String boundManagedObjectName, String officeManagedObjectName) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.officeManagedObjectName = officeManagedObjectName;
	}

	/**
	 * Initiate as an {@link InputManagedObjectConfiguration}.
	 * 
	 * @param boundManagedObjectName Name of the {@link ManagedObject} is being
	 *                               bound.
	 */
	public DependencyMappingBuilderImpl(String boundManagedObjectName) {
		this(boundManagedObjectName, null);
	}

	/**
	 * Maps in the dependency.
	 * 
	 * @param index                  Index to map the dependency under.
	 * @param key                    Key for the dependency. May be
	 *                               <code>null</code>.
	 * @param scopeManagedObjectName Scope name for the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	private <d extends Enum<d>> void mapDependency(int index, d key, String scopeManagedObjectName) {

		// Cast key to expected type
		O castKey = (O) key;

		// Create the dependency
		ManagedObjectDependencyConfigurationImpl dependency = new ManagedObjectDependencyConfigurationImpl(castKey,
				scopeManagedObjectName);

		// Map the dependency at the index
		this.dependencies.put(Integer.valueOf(index), dependency);
	}

	/*
	 * ============= DependencyMappingBuilder =============================
	 */

	@Override
	public <d extends Enum<d>> void mapDependency(d key, String scopeManagedObjectName) {
		// Use ordinal of key to index the dependency
		this.mapDependency(key.ordinal(), key, scopeManagedObjectName);
	}

	@Override
	public void mapDependency(int index, String scopeManagedObjectName) {
		this.mapDependency(index, (O) null, scopeManagedObjectName);
	}

	@Override
	public void mapGovernance(String governanceName) {

		// Create the governance
		ManagedObjectGovernanceConfigurationImpl governance = new ManagedObjectGovernanceConfigurationImpl(
				governanceName);

		// Add the governance
		this.governances.add(governance);
	}

	@Override
	public <E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preLoadAdminister(
			String administrationName, Class<E> extension, AdministrationFactory<E, f, G> administrationFactory) {
		AdministrationBuilderImpl<E, f, G> admin = new AdministrationBuilderImpl<>(administrationName, extension,
				administrationFactory);
		this.preLoadAdministrations.add(admin);
		return admin;
	}

	/*
	 * ========== ThreadDependencyMappingBuilder ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <T> OptionalThreadLocal<T> getOptionalThreadLocal() {

		// Ensure have thread local
		if (this.threadLocal == null) {
			this.threadLocal = new ThreadLocalImpl<>();
		}

		// Return the optional thread local
		return (OptionalThreadLocal<T>) this.threadLocal.getOptionalThreadLocal();
	}

	/*
	 * ==== ManagedObjectConfiguration & InputManagedObjectConfiguration ====
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	@Override
	public String getOfficeManagedObjectName() {
		return this.officeManagedObjectName;
	}

	@Override
	public ManagedObjectDependencyConfiguration<O>[] getDependencyConfiguration() {
		return ConstructUtil.toArray(this.dependencies, new ManagedObjectDependencyConfiguration[0]);
	}

	@Override
	public ManagedObjectGovernanceConfiguration[] getGovernanceConfiguration() {
		return (ManagedObjectGovernanceConfiguration[]) this.governances
				.toArray(new ManagedObjectGovernanceConfiguration[this.governances.size()]);
	}

	@Override
	public AdministrationConfiguration<?, ?, ?>[] getPreLoadAdministration() {
		return this.preLoadAdministrations.toArray(new AdministrationConfiguration[0]);
	}

	@Override
	public ThreadLocalConfiguration getThreadLocalConfiguration() {
		return this.threadLocal;
	}

	/**
	 * {@link ManagedObjectDependencyConfiguration} implementation.
	 */
	private class ManagedObjectDependencyConfigurationImpl implements ManagedObjectDependencyConfiguration<O> {

		/**
		 * Dependency key.
		 */
		private final O dependencyKey;

		/**
		 * {@link ManagedObject} name.
		 */
		private final String managedObjectName;

		/**
		 * Initiate.
		 * 
		 * @param dependencyKey     Dependency key.
		 * @param managedObjectName {@link ManagedObject} name.
		 */
		public ManagedObjectDependencyConfigurationImpl(O dependencyKey, String managedObjectName) {
			this.dependencyKey = dependencyKey;
			this.managedObjectName = managedObjectName;
		}

		/*
		 * ============= ManagedObjectDependencyConfiguration ================
		 */

		@Override
		public O getDependencyKey() {
			return this.dependencyKey;
		}

		@Override
		public String getScopeManagedObjectName() {
			return this.managedObjectName;
		}
	}

	/**
	 * {@link ManagedObjectGovernanceConfiguration} implementation.
	 */
	private class ManagedObjectGovernanceConfigurationImpl implements ManagedObjectGovernanceConfiguration {

		/**
		 * {@link Governance} name.
		 */
		private final String governanceName;

		/**
		 * Initiate.
		 * 
		 * @param governanceName {@link Governance} name.
		 */
		public ManagedObjectGovernanceConfigurationImpl(String governanceName) {
			this.governanceName = governanceName;
		}

		/*
		 * ============= ManagedObjectGovernanceConfiguration ================
		 */

		@Override
		public String getGovernanceName() {
			return this.governanceName;
		}
	}

}
