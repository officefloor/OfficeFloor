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

package net.officefloor.plugin.section.transform;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.compile.spi.section.SectionDependencyRequireNode;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SectionDesigner} to intercept configuration to provide access to the
 * {@link SectionSource} configured items.
 * 
 * @author Daniel Sagenschneider
 */
public class TransformSectionDesigner implements SectionDesigner {

	/**
	 * Registers the item.
	 * 
	 * @param itemName
	 *            Name of the item.
	 * @param items
	 *            {@link Map} of item instances by their name.
	 * @param item
	 *            Item.
	 * @return Registered item.
	 */
	private static <T> T register(String itemName, Map<String, T> items, T item) {
		items.put(itemName, item);
		return item;
	}

	/**
	 * Delegate {@link SectionDesigner}.
	 */
	private final SectionDesigner delegate;

	/**
	 * {@link SectionInput} instances by their name.
	 */
	private final Map<String, SectionInput> inputs = new HashMap<String, SectionInput>();

	/**
	 * {@link SectionOutput} instances by their name.
	 */
	private final Map<String, SectionOutput> outputs = new HashMap<String, SectionOutput>();

	/**
	 * {@link SectionObject} instances by their name.
	 */
	private final Map<String, SectionObject> objects = new HashMap<String, SectionObject>();

	/**
	 * {@link SubSection} instances by their name.
	 */
	private final Map<String, SubSection> subSections = new HashMap<String, SubSection>();

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link SectionDesigner}.
	 */
	public TransformSectionDesigner(SectionDesigner delegate) {
		this.delegate = delegate;
	}

	/**
	 * Obtains the {@link SectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @return {@link SectionInput}.
	 */
	public SectionInput getSectionInput(String inputName) {
		return this.inputs.get(inputName);
	}

	/**
	 * Obtains the {@link SectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutput}.
	 * @return {@link SectionOutput}.
	 */
	public SectionOutput getSectionOutput(String outputName) {
		return this.outputs.get(outputName);
	}

	/**
	 * Obtains the {@link SubSection}.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	public SubSection getSubSection(String subSectionName) {
		return this.subSections.get(subSectionName);
	}

	/*
	 * ==================== SectionDesigner ========================
	 */

	@Override
	public SectionInput addSectionInput(String inputName, String parameterType) {
		return register(inputName, this.inputs, this.delegate.addSectionInput(inputName, parameterType));
	}

	@Override
	public SectionOutput addSectionOutput(String outputName, String argumentType, boolean isEscalationOnly) {
		return register(outputName, this.outputs,
				this.delegate.addSectionOutput(outputName, argumentType, isEscalationOnly));
	}

	@Override
	public SectionObject addSectionObject(String objectName, String objectType) {
		return register(objectName, this.objects, this.delegate.addSectionObject(objectName, objectType));
	}

	@Override
	public SectionFunctionNamespace addSectionFunctionNamespace(String namespaceName,
			String managedFunctionSourceClassName) {
		return this.delegate.addSectionFunctionNamespace(namespaceName, managedFunctionSourceClassName);
	}

	@Override
	public SectionFunctionNamespace addSectionFunctionNamespace(String namespaceName,
			ManagedFunctionSource managedFunctionSource) {
		return this.delegate.addSectionFunctionNamespace(namespaceName, managedFunctionSource);
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return this.delegate.addSectionManagedObjectSource(managedObjectSourceName, managedObjectSourceClassName);
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return this.delegate.addSectionManagedObjectSource(managedObjectSourceName, managedObjectSource);
	}

	@Override
	public SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName) {
		return this.delegate.addManagedObjectPool(managedObjectPoolName, managedObjectPoolSourceClassName);
	}

	@Override
	public SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource) {
		return this.delegate.addManagedObjectPool(managedObjectPoolName, managedObjectPoolSource);
	}

	@Override
	public SubSection addSubSection(String subSectionName, String sectionSourceClassName, String location) {
		return register(subSectionName, this.subSections,
				this.delegate.addSubSection(subSectionName, sectionSourceClassName, location));
	}

	@Override
	public SubSection addSubSection(String subSectionName, SectionSource sectionSource, String location) {
		return register(subSectionName, this.subSections,
				this.delegate.addSubSection(subSectionName, sectionSource, location));
	}

	@Override
	public void link(SectionManagedObjectSource managedObjectSource, SectionManagedObjectPool managedObjectPool) {
		this.delegate.link(managedObjectSource, managedObjectPool);
	}

	@Override
	public void link(SectionFlowSourceNode flowSourceNode, SectionFlowSinkNode flowSinkNode) {
		this.delegate.link(flowSourceNode, flowSinkNode);
	}

	@Override
	public void link(FunctionFlow functionFlow, SectionFlowSinkNode sectionSinkNode, boolean isSpawnThreadState) {
		this.delegate.link(functionFlow, sectionSinkNode, isSpawnThreadState);
	}

	@Override
	public void link(SectionDependencyRequireNode sectionRequireNode, SectionDependencyObjectNode sectionObjectNode) {
		this.delegate.link(sectionRequireNode, sectionObjectNode);
	}

	@Override
	public CompileError addIssue(String issueDescription) {
		return this.delegate.addIssue(issueDescription);
	}

	@Override
	public CompileError addIssue(String issueDescription, Throwable cause) {
		return this.delegate.addIssue(issueDescription, cause);
	}

}
