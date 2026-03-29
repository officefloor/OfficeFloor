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

package net.officefloor.frame.impl.construct.managedfunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.administration.AdministrationBuilderImpl;
import net.officefloor.frame.impl.construct.function.AbstractFunctionBuilder;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.FunctionState;

/**
 * Implementation of the {@link ManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionBuilderImpl<O extends Enum<O>, F extends Enum<F>> extends AbstractFunctionBuilder<F>
		implements ManagedFunctionBuilder<O, F>, ManagedFunctionConfiguration<O, F> {

	/**
	 * Name of this {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<O, F> functionFactory;

	/**
	 * {@link Object} instances to be linked to this {@link ManagedFunction}.
	 */
	private final Map<Integer, ManagedFunctionObjectConfigurationImpl<O>> objects = new HashMap<Integer, ManagedFunctionObjectConfigurationImpl<O>>();

	/**
	 * {@link Governance} instances to be active for this {@link ManagedFunction}.
	 */
	private final List<ManagedFunctionGovernanceConfiguration> governances = new LinkedList<ManagedFunctionGovernanceConfiguration>();

	/**
	 * Annotations.
	 */
	private List<Object> annotations = new LinkedList<>();

	/**
	 * Listing of {@link ManagedFunction} bound {@link ManagedObject} configuration.
	 */
	private final List<ManagedObjectConfiguration<?>> functionManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Next {@link FunctionState}.
	 */
	private ManagedFunctionReference nextFunction;

	/**
	 * Listing of {@link Administration} to do before executing the
	 * {@link ManagedFunction}.
	 */
	private final List<AdministrationConfiguration<?, ?, ?>> preAdministration = new LinkedList<AdministrationConfiguration<?, ?, ?>>();

	/**
	 * Listing of {@link Administration} to do after executing the
	 * {@link ManagedFunction}.
	 */
	private final List<AdministrationConfiguration<?, ?, ?>> postAdministration = new LinkedList<AdministrationConfiguration<?, ?, ?>>();

	/**
	 * {@link AsynchronousFlow} timeout.
	 */
	private long asynchronousFlowTimeout = -1;

	/**
	 * Initiate.
	 * 
	 * @param functionName    Name of this {@link ManagedFunction}.
	 * @param functionFactory {@link ManagedFunctionFactory}.
	 */
	public ManagedFunctionBuilderImpl(String functionName, ManagedFunctionFactory<O, F> functionFactory) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
	}

	/*
	 * ======================= ManagedFunctionBuilder =======================
	 */

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
	}

	@Override
	public void setNextFunction(String functionName, Class<?> argumentType) {
		this.nextFunction = new ManagedFunctionReferenceImpl(functionName, argumentType);
	}

	@Override
	public void linkParameter(O key, Class<?> parameterType) {
		this.linkObject(key.ordinal(), key, true, null, parameterType);
	}

	@Override
	public void linkParameter(int index, Class<?> parameterType) {
		this.linkObject(index, null, true, null, parameterType);
	}

	@Override
	public void linkManagedObject(O key, String scopeManagedObjectName, Class<?> objectType) {
		this.linkObject(key.ordinal(), key, false, scopeManagedObjectName, objectType);
	}

	@Override
	public void linkManagedObject(int index, String scopeManagedObjectName, Class<?> objectType) {
		this.linkObject(index, null, false, scopeManagedObjectName, objectType);
	}

	/**
	 * Links in a dependent {@link Object}.
	 * 
	 * @param objectIndex            Index of the {@link Object}.
	 * @param objectKey              Key of the {@link Object}. Should be
	 *                               <code>null</code> if indexed.
	 * @param isParameter            <code>true</code> if the {@link Object} is a
	 *                               parameter.
	 * @param scopeManagedObjectName Name of the {@link ManagedObject} within the
	 *                               {@link ManagedObjectSource}. Should be
	 *                               <code>null</code> if a parameter.
	 * @param objectType             Type of {@link Object} required.
	 */
	private void linkObject(int objectIndex, O objectKey, boolean isParameter, String scopeManagedObjectName,
			Class<?> objectType) {
		this.objects.put(Integer.valueOf(objectIndex), new ManagedFunctionObjectConfigurationImpl<O>(isParameter,
				scopeManagedObjectName, objectType, objectIndex, objectKey));
	}

	@Override
	public DependencyMappingBuilder addManagedObject(String functionManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl<>(functionManagedObjectName,
				officeManagedObjectName);
		this.functionManagedObjects.add(builder);
		return builder;
	}

	@Override
	public <E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preAdminister(
			String administrationName, Class<E> extension, AdministrationFactory<E, f, G> administrationFactory) {
		AdministrationBuilderImpl<E, f, G> builder = new AdministrationBuilderImpl<>(administrationName, extension,
				administrationFactory);
		this.preAdministration.add(builder);
		return builder;
	}

	@Override
	public <E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> postAdminister(
			String administrationName, Class<E> extension, AdministrationFactory<E, f, G> administrationFactory) {
		AdministrationBuilderImpl<E, f, G> builder = new AdministrationBuilderImpl<>(administrationName, extension,
				administrationFactory);
		this.postAdministration.add(builder);
		return builder;
	}

	@Override
	public void addGovernance(String governanceName) {
		this.governances.add(new ManagedFunctionGovernanceConfigurationImpl(governanceName));
	}

	@Override
	public void setAsynchronousFlowTimeout(long timeout) {
		this.asynchronousFlowTimeout = timeout;
	}

	/*
	 * ============ TaskConfiguration =====================================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public ManagedFunctionFactory<O, F> getManagedFunctionFactory() {
		return this.functionFactory;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations.toArray(new Object[this.annotations.size()]);
	}

	@Override
	public ManagedFunctionObjectConfiguration<O>[] getObjectConfiguration() {
		return ConstructUtil.toArray(this.objects, new ManagedFunctionObjectConfiguration[0]);
	}

	@Override
	public ManagedFunctionGovernanceConfiguration[] getGovernanceConfiguration() {
		return this.governances.toArray(new ManagedFunctionGovernanceConfiguration[this.governances.size()]);
	}

	@Override
	public ManagedFunctionReference getNextFunction() {
		return this.nextFunction;
	}

	@Override
	public AdministrationConfiguration<?, ?, ?>[] getPreAdministration() {
		return ConstructUtil.toArray(this.preAdministration, new AdministrationConfiguration[0]);
	}

	@Override
	public AdministrationConfiguration<?, ?, ?>[] getPostAdministration() {
		return ConstructUtil.toArray(this.postAdministration, new AdministrationConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getManagedObjectConfiguration() {
		return this.functionManagedObjects.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public long getAsynchronousFlowTimeout() {
		return this.asynchronousFlowTimeout;
	}

}
