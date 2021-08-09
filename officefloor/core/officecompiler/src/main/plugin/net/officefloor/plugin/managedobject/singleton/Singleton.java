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

package net.officefloor.plugin.managedobject.singleton;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Provides a singleton object.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class Singleton extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, ExtensionFactory<Object> {

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param deployer       {@link OfficeFloorDeployer}.
	 * @param singleton      Singleton {@link Object}.
	 * @param managingOffice Managing {@link DeployedOffice}.
	 * @param autoWires      Optional {@link AutoWire} instances for the singleton.
	 * @return {@link OfficeFloorManagedObject} for the singleton.
	 */
	public static OfficeFloorManagedObject load(OfficeFloorDeployer deployer, Object singleton,
			DeployedOffice managingOffice, AutoWire... autoWires) {
		return load(deployer, null, singleton, managingOffice, autoWires);
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param deployer          {@link OfficeFloorDeployer}.
	 * @param managedObjectName Name of {@link OfficeManagedObject}.
	 * @param singleton         Singleton {@link Object}.
	 * @param managingOffice    Managing {@link DeployedOffice}.
	 * @param autoWires         Optional {@link AutoWire} instances for the
	 *                          singleton.
	 * @return {@link OfficeFloorManagedObject} for the singleton.
	 */
	public static OfficeFloorManagedObject load(OfficeFloorDeployer deployer, String managedObjectName,
			Object singleton, DeployedOffice managingOffice, AutoWire... autoWires) {

		// Ensure have a managed object name
		if (managedObjectName == null) {
			managedObjectName = singleton.getClass().getSimpleName();
		}

		// Load the managed object source
		OfficeFloorManagedObjectSource managedObjectSource = deployer.addManagedObjectSource(managedObjectName,
				new Singleton(singleton));
		deployer.link(managedObjectSource.getManagingOffice(), managingOffice);

		// Load the managed object
		OfficeFloorManagedObject managedObject = managedObjectSource.addOfficeFloorManagedObject(managedObjectName,
				ManagedObjectScope.PROCESS);

		// Load auto wire information to managed object
		for (AutoWire autoWire : autoWires) {
			managedObject.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
		}

		// Return the managed object
		return managedObject;
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param architect {@link OfficeArchitect}.
	 * @param singleton Singleton {@link Object}.
	 * @param autoWires Optional {@link AutoWire} instances for the singleton.
	 * @return {@link OfficeManagedObject} for the singleton.
	 */
	public static OfficeManagedObject load(OfficeArchitect architect, Object singleton, AutoWire... autoWires) {
		return load(architect, null, singleton, autoWires);
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param architect         {@link OfficeArchitect}.
	 * @param managedObjectName Name of {@link OfficeManagedObject}.
	 * @param singleton         Singleton {@link Object}.
	 * @param autoWires         Optional {@link AutoWire} instances for the
	 *                          singleton.
	 * @return {@link OfficeManagedObject} for the singleton.
	 */
	public static OfficeManagedObject load(OfficeArchitect architect, String managedObjectName, Object singleton,
			AutoWire... autoWires) {

		// Ensure have a managed object name
		if (managedObjectName == null) {
			managedObjectName = singleton.getClass().getSimpleName();
		}

		// Load the managed object source
		OfficeManagedObjectSource managedObjectSource = architect.addOfficeManagedObjectSource(managedObjectName,
				new Singleton(singleton));

		// Load the managed object
		OfficeManagedObject managedObject = managedObjectSource.addOfficeManagedObject(managedObjectName,
				ManagedObjectScope.PROCESS);

		// Load auto wire information to managed object
		for (AutoWire autoWire : autoWires) {
			managedObject.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
		}

		// Return the managed object
		return managedObject;
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param designer  {@link SectionDesigner}.
	 * @param singleton Singleton {@link Object}.
	 * @return {@link SectionManagedObject}.
	 */
	public static SectionManagedObject load(SectionDesigner designer, Object singleton) {
		return load(designer, null, singleton);
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param designer          {@link SectionDesigner}.
	 * @param managedObjectName Name of {@link SectionManagedObject}.
	 * @param singleton         Singleton {@link Object}.
	 * @return {@link SectionManagedObject}.
	 */
	public static SectionManagedObject load(SectionDesigner designer, String managedObjectName, Object singleton) {

		// Ensure have a managed object name
		if (managedObjectName == null) {
			managedObjectName = singleton.getClass().getSimpleName();
		}

		// Load the managed object source
		SectionManagedObjectSource managedObjectSource = designer.addSectionManagedObjectSource(managedObjectName,
				new Singleton(singleton));

		// Load the managed object
		SectionManagedObject managedObject = managedObjectSource.addSectionManagedObject(managedObjectName,
				ManagedObjectScope.PROCESS);

		// Return the managed object
		return managedObject;
	}

	/**
	 * Singleton.
	 */
	private final Object object;

	/**
	 * Initiate.
	 * 
	 * @param object Singleton object.
	 */
	public Singleton(Object object) {
		this.object = object;
	}

	/*
	 * ========================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Obtain the object type
		Class<?> objectType = this.object.getClass();

		// Specify the object type
		context.setObjectClass(objectType);

		// Load the extension interfaces
		List<Class<?>> extensionInterfaces = new LinkedList<Class<?>>();
		this.loadAllExtensionInterfaces(objectType, extensionInterfaces);
		for (Class extensionInterface : extensionInterfaces) {
			context.addManagedObjectExtension(extensionInterface, this);
		}
	}

	@Override
	protected ManagedObject getManagedObject() {
		return this;
	}

	/*
	 * ============================ ManagedObject ============================
	 */

	@Override
	public Object getObject() {
		return this.object;
	}

	/*
	 * =================== ExtensionFactory ======================
	 */

	@Override
	public Object createExtension(ManagedObject managedObject) {
		return ((Singleton) managedObject).object;
	}

	/**
	 * Loads all extension interfaces from type.
	 * 
	 * @param type       Type.
	 * @param interfaces Listing of interfaces to be loaded.
	 */
	private void loadAllExtensionInterfaces(Class<?> type, List<Class<?>> interfaces) {

		// Determine if already loaded type
		if (interfaces.contains(type)) {
			return; // already loaded information for type
		}

		// Add type to prevent it further being loaded
		interfaces.add(type);

		// Load the interfaces from type
		for (Class<?> interfaceType : type.getInterfaces()) {
			this.loadAllExtensionInterfaces(interfaceType, interfaces);
		}

		// Load parent class types
		Class<?> parentType = type.getSuperclass();
		if (parentType != null) {
			this.loadAllExtensionInterfaces(parentType, interfaces);
		}
	}

}
