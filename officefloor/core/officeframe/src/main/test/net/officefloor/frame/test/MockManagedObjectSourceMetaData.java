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
package net.officefloor.frame.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectDependencyMetaDataImpl;
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
	 * Initiate from the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public MockManagedObjectSourceMetaData(ManagedObject managedObject) {
		this.managedObjectClass = managedObject.getClass();
		try {
			this.objectClass = managedObject.getObject().getClass();
		} catch (Throwable ex) {
			TestCase.fail("Failed to obtain object type from managed object "
					+ ex.getMessage());
			throw new Error("Only for compiling as fail above will throw");
		}
		this.dependencyMetaData = null;
		this.flowMetaData = null;
	}

	/**
	 * Initiate.
	 *
	 * @param <MO>
	 *            {@link ManagedObject} type.
	 * @param managedObjectClass
	 *            Class of the {@link ManagedObject}.
	 * @param objectClass
	 *            Class of the object being managed.
	 * @param dependencyKeys
	 *            Dependency key {@link Enum}.
	 * @param dependencyClasses
	 *            {@link Class} types for the dependency keys.
	 * @param flowKeys
	 *            Flow key {@link Enum}.
	 * @param flowClasses
	 *            {@link Class} types for the arguments of the flow keys.
	 */
	@SuppressWarnings("unchecked")
	public <MO extends ManagedObject> MockManagedObjectSourceMetaData(
			Class<MO> managedObjectClass, Class<?> objectClass,
			Class<D> dependencyKeys, Map<D, Class<?>> dependencyClasses,
			Class<H> flowKeys, Map<H, Class<?>> flowClasses) {
		this.managedObjectClass = managedObjectClass;
		this.objectClass = objectClass;

		// Load the dependency meta-data
		D[] keysForDependencies = dependencyKeys.getEnumConstants();
		this.dependencyMetaData = new ManagedObjectDependencyMetaData[keysForDependencies.length];
		for (int i = 0; i < keysForDependencies.length; i++) {
			D keyForDependency = keysForDependencies[i];
			this.dependencyMetaData[i] = new ManagedObjectDependencyMetaDataImpl<D>(
					keyForDependency, dependencyClasses.get(keyForDependency));
		}

		// Load the flow meta-data
		H[] keysForHandlers = flowKeys.getEnumConstants();
		this.flowMetaData = new ManagedObjectFlowMetaData[keysForHandlers.length];
		for (int i = 0; i < keysForHandlers.length; i++) {
			H keyForHandler = keysForHandlers[i];
			this.flowMetaData[i] = new ManagedObjectFlowMetaDataImpl<H>(
					keyForHandler, flowClasses.get(keyForHandler));
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
		// Use interfaces of object
		List<ManagedObjectExtensionInterfaceMetaData<?>> metaData = new LinkedList<ManagedObjectExtensionInterfaceMetaData<?>>();
		for (Class<?> type : this.objectClass.getInterfaces()) {
			metaData.add(new MockManagedObjectExtensionInterfaceMetaData(type));
		}

		// Return the extension interface meta-data
		return metaData.toArray(new ManagedObjectExtensionInterfaceMetaData[0]);
	}

	/**
	 * Mock {@link ManagedObjectExtensionInterfaceMetaData}.
	 */
	private class MockManagedObjectExtensionInterfaceMetaData<I> implements
			ManagedObjectExtensionInterfaceMetaData<I>,
			ExtensionInterfaceFactory<I> {

		/**
		 * Extension interface type.
		 */
		private final Class<I> extensionInterfaceType;

		/**
		 * Initiate.
		 * 
		 * @param extensionInterfaceType
		 *            Extension interface type.
		 */
		public MockManagedObjectExtensionInterfaceMetaData(
				Class<I> extensionInterfaceType) {
			this.extensionInterfaceType = extensionInterfaceType;
		}

		/*
		 * ============== ManagedObjectExtensionInterfaceMetaData ===========
		 */

		@Override
		public Class<I> getExtensionInterfaceType() {
			return this.extensionInterfaceType;
		}

		@Override
		public ExtensionInterfaceFactory<I> getExtensionInterfaceFactory() {
			return this;
		}

		/*
		 * ================ ExtensionInterfaceFactory ========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public I createExtensionInterface(ManagedObject managedObject) {
			try {
				return (I) managedObject.getObject();
			} catch (Throwable ex) {
				throw OfficeFrameTestCase.fail(ex);
			}
		}
	}

}