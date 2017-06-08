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
package net.officefloor.plugin.managedobject.singleton;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Provides a singleton object.
 * 
 * @author Daniel Sagenschneider
 */
public class Singleton extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, ExtensionInterfaceFactory<Object> {

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param architect
	 *            {@link OfficeArchitect}.
	 * @param singleton
	 *            Singleton {@link Object}.
	 * @param autoWires
	 *            Optional {@link AutoWire} instances for the singleton.
	 * @return {@link OfficeManagedObject} for the singleton.
	 */
	public static OfficeManagedObject load(OfficeArchitect architect, Object singleton, AutoWire... autoWires) {
		return load(architect, null, singleton, autoWires);
	}

	/**
	 * Convenience method to load singleton {@link Object}.
	 * 
	 * @param architect
	 *            {@link OfficeArchitect}.
	 * @param managedObjectName
	 *            Name of {@link OfficeManagedObject}.
	 * @param singleton
	 *            Singleton {@link Object}.
	 * @param autoWires
	 *            Optional {@link AutoWire} instances for the singleton.
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
	 * Singleton.
	 */
	private final Object object;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Singleton object.
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
			context.addManagedObjectExtensionInterface(extensionInterface, this);
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
	 * =================== ExtensionInterfaceFactory ======================
	 */

	@Override
	public Object createExtensionInterface(ManagedObject managedObject) {
		return ((Singleton) managedObject).object;
	}

	/**
	 * Loads all extension interfaces from type.
	 * 
	 * @param type
	 *            Type.
	 * @param interfaces
	 *            Listing of interfaces to be loaded.
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