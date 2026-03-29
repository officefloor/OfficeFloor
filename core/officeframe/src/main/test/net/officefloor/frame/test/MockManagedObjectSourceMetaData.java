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

package net.officefloor.frame.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.TestCase;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectDependencyMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectExecutionMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectFlowMetaDataImpl;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Mock {@link ManagedObjectSourceMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockManagedObjectSourceMetaData<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSourceMetaData<D, H> {

	/**
	 * {@link Class} of the {@link ManagedObject}.
	 */
	private final Class<? extends ManagedObject> managedObjectClass;

	/**
	 * Class of object being managed.
	 */
	private final Class<?> objectClass;

	/**
	 * Dependency meta-data.
	 */
	private final ManagedObjectDependencyMetaData<D>[] dependencyMetaData;

	/**
	 * {@link Flow} meta-data.
	 */
	private final ManagedObjectFlowMetaData<H>[] flowMetaData;

	/**
	 * {@link ExecutionStrategy} meta-data.
	 */
	private final ManagedObjectExecutionMetaData[] executionMetaData;

	/**
	 * Initiate from the {@link ManagedObject}.
	 * 
	 * @param managedObject {@link ManagedObject}.
	 */
	public MockManagedObjectSourceMetaData(ManagedObject managedObject) {
		this.managedObjectClass = managedObject.getClass();
		try {
			this.objectClass = managedObject.getObject().getClass();
		} catch (Throwable ex) {
			TestCase.fail("Failed to obtain object type from managed object " + ex.getMessage());
			throw new Error("Only for compiling as fail above will throw");
		}
		this.dependencyMetaData = null;
		this.flowMetaData = null;
		this.executionMetaData = null;
	}

	/**
	 * Initiate.
	 *
	 * @param                     <MO> {@link ManagedObject} type.
	 * @param managedObjectClass  Class of the {@link ManagedObject}.
	 * @param objectClass         Class of the object being managed.
	 * @param dependencyKeys      Dependency key {@link Enum}.
	 * @param dependencyClasses   {@link Class} types for the dependency keys.
	 * @param flowKeys            Flow key {@link Enum}.
	 * @param flowClasses         {@link Class} types for the arguments of the flow
	 *                            keys.
	 * @param executionStrategies Names of the {@link ExecutionStrategy} instances.
	 */
	@SuppressWarnings("unchecked")
	public <MO extends ManagedObject> MockManagedObjectSourceMetaData(Class<MO> managedObjectClass,
			Class<?> objectClass, Class<D> dependencyKeys, Map<D, Class<?>> dependencyClasses, Class<H> flowKeys,
			Map<H, Class<?>> flowClasses, String[] executionStrategies) {
		this.managedObjectClass = managedObjectClass;
		this.objectClass = objectClass;

		// Load the dependency meta-data
		D[] keysForDependencies = dependencyKeys.getEnumConstants();
		this.dependencyMetaData = new ManagedObjectDependencyMetaData[keysForDependencies.length];
		for (int i = 0; i < keysForDependencies.length; i++) {
			D keyForDependency = keysForDependencies[i];
			this.dependencyMetaData[i] = new ManagedObjectDependencyMetaDataImpl<D>(keyForDependency,
					dependencyClasses.get(keyForDependency));
		}

		// Load the flow meta-data
		H[] keysForHandlers = flowKeys.getEnumConstants();
		this.flowMetaData = new ManagedObjectFlowMetaData[keysForHandlers.length];
		for (int i = 0; i < keysForHandlers.length; i++) {
			H keyForHandler = keysForHandlers[i];
			this.flowMetaData[i] = new ManagedObjectFlowMetaDataImpl<H>(keyForHandler, flowClasses.get(keyForHandler));
		}

		// Load the execution strategies
		this.executionMetaData = new ManagedObjectExecutionMetaData[executionStrategies.length];
		Function<String, ManagedObjectExecutionMetaData> createExecution = (label) -> {
			ManagedObjectExecutionMetaDataImpl execution = new ManagedObjectExecutionMetaDataImpl();
			execution.setLabel(label);
			return execution;
		};
		for (int i = 0; i < executionStrategies.length; i++) {
			this.executionMetaData[i] = createExecution.apply(executionStrategies[i]);
		}
	}

	/*
	 * ==================== ManagedObjectSourceMetaData =======================
	 */

	@Override
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return this.managedObjectClass;
	}

	@Override
	public Class<?> getObjectClass() {
		return this.objectClass;
	}

	@Override
	public ManagedObjectDependencyMetaData<D>[] getDependencyMetaData() {
		return this.dependencyMetaData;
	}

	@Override
	public ManagedObjectFlowMetaData<H>[] getFlowMetaData() {
		return this.flowMetaData;
	}

	@Override
	public ManagedObjectExecutionMetaData[] getExecutionMetaData() {
		return this.executionMetaData;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData() {
		// Use interfaces of object
		List<ManagedObjectExtensionMetaData<?>> metaData = new LinkedList<ManagedObjectExtensionMetaData<?>>();
		for (Class<?> type : this.objectClass.getInterfaces()) {
			metaData.add(new MockManagedObjectExtensionInterfaceMetaData(type));
		}

		// Return the extension interface meta-data
		return metaData.toArray(new ManagedObjectExtensionMetaData[0]);
	}

	/**
	 * Mock {@link ManagedObjectExtensionMetaData}.
	 */
	private class MockManagedObjectExtensionInterfaceMetaData<I>
			implements ManagedObjectExtensionMetaData<I>, ExtensionFactory<I> {

		/**
		 * Extension interface type.
		 */
		private final Class<I> extensionInterfaceType;

		/**
		 * Initiate.
		 * 
		 * @param extensionInterfaceType Extension interface type.
		 */
		public MockManagedObjectExtensionInterfaceMetaData(Class<I> extensionInterfaceType) {
			this.extensionInterfaceType = extensionInterfaceType;
		}

		/*
		 * ============== ManagedObjectExtensionInterfaceMetaData ===========
		 */

		@Override
		public Class<I> getExtensionType() {
			return this.extensionInterfaceType;
		}

		@Override
		public ExtensionFactory<I> getExtensionFactory() {
			return this;
		}

		/*
		 * ================ ExtensionInterfaceFactory ========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public I createExtension(ManagedObject managedObject) {
			try {
				return (I) managedObject.getObject();
			} catch (Throwable ex) {
				throw OfficeFrameTestCase.fail(ex);
			}
		}
	}

}
