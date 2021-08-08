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

package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.function.ManagedObjectFunctionEnhancer;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedobjectpool.ManagedObjectPoolBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;

/**
 * Implements the {@link ManagedObjectBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectBuilderImpl<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>>
		implements ManagedObjectBuilder<F>, ManagedObjectSourceConfiguration<F, MS> {

	/**
	 * Name of {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link ManagedObjectSource} instance.
	 */
	private final MS managedObjectSourceInstance;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	private final Class<MS> managedObjectSourceClass;

	/**
	 * {@link ManagingOfficeConfiguration} for this {@link ManagedObject}.
	 */
	private ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * Additional profiles.
	 */
	private final List<String> additionalProfiles = new LinkedList<>();

	/**
	 * {@link SourceProperties} for the {@link ManagedObjectSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link ManagedObjectFunctionEnhancer} instances.
	 */
	private final List<ManagedObjectFunctionEnhancer> functionEnhancers = new LinkedList<>();

	/**
	 * Start up before.
	 */
	private final List<String> startupBefores = new LinkedList<>();

	/**
	 * Start up after.
	 */
	private final List<String> startupAfters = new LinkedList<>();

	/**
	 * {@link ManagedObjectPoolConfiguration}.
	 */
	private ManagedObjectPoolConfiguration poolConfiguration;

	/**
	 * Timeout for {@link AsynchronousManagedObject}.
	 */
	private long timeout = 0;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName  Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass {@link Class} of the
	 *                                 {@link ManagedObjectSource}.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName, Class<MS> managedObjectSourceClass) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceInstance = null;
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instance to use.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName, MS managedObjectSource) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceInstance = managedObjectSource;
		this.managedObjectSourceClass = null;
	}

	/*
	 * ================= ManagedObjectBuilder =============================
	 */

	@Override
	public void addAdditionalProfile(String profile) {
		this.additionalProfiles.add(profile);
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	@Override
	public ManagedObjectPoolBuilder setManagedObjectPool(ManagedObjectPoolFactory managedObjectPoolFactory) {
		ManagedObjectPoolBuilderImpl poolBuilder = new ManagedObjectPoolBuilderImpl(managedObjectPoolFactory);
		this.poolConfiguration = poolBuilder;
		return poolBuilder;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public ManagingOfficeBuilder<F> setManagingOffice(String officeName) {
		ManagingOfficeBuilderImpl<F> managingOfficeBuilder = new ManagingOfficeBuilderImpl<F>(officeName);
		this.managingOfficeConfiguration = managingOfficeBuilder;
		return managingOfficeBuilder;
	}

	@Override
	public void addFunctionEnhancer(ManagedObjectFunctionEnhancer functionEnhancer) {
		this.functionEnhancers.add(functionEnhancer);
	}

	@Override
	public void startupBefore(String managedObjectSourceName) {
		this.startupBefores.add(managedObjectSourceName);
	}

	@Override
	public void startupAfter(String managedObjectSourceName) {
		this.startupAfters.add(managedObjectSourceName);
	}

	/*
	 * ================= ManagedObjectSourceConfiguration =================
	 */

	@Override
	public String getManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public ManagingOfficeConfiguration<F> getManagingOfficeConfiguration() {
		return this.managingOfficeConfiguration;
	}

	@Override
	public MS getManagedObjectSource() {
		return this.managedObjectSourceInstance;
	}

	@Override
	public Class<MS> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	@Override
	public String[] getAdditionalProfiles() {
		return this.additionalProfiles.toArray(new String[this.additionalProfiles.size()]);
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

	@Override
	public ManagedObjectPoolConfiguration getManagedObjectPoolConfiguration() {
		return this.poolConfiguration;
	}

	@Override
	public ManagedObjectFunctionEnhancer[] getManagedObjectFunctionEnhancers() {
		return this.functionEnhancers.toArray(new ManagedObjectFunctionEnhancer[this.functionEnhancers.size()]);
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public String[] getStartupBefore() {
		return this.startupBefores.toArray(new String[this.startupBefores.size()]);
	}

	@Override
	public String[] getStartupAfter() {
		return this.startupAfters.toArray(new String[this.startupAfters.size()]);
	}

}
