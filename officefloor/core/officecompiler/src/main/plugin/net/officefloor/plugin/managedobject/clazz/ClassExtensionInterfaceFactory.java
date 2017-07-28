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
package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;

/**
 * <p>
 * {@link ExtensionInterfaceFactory} that return the object of the
 * {@link ClassManagedObject}.
 * <p>
 * This allows any implemented interfaces of the class to be an extension
 * interface for the {@link ManagedObject} with implementation delegated to the
 * object instantiated from the class.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ClassExtensionInterfaceFactory implements
		ExtensionInterfaceFactory {

	/**
	 * Registers the extension interface.
	 * 
	 * @param context
	 *            {@link MetaDataContext} to add the extension interface.
	 * @param objectClass
	 *            Object class which is the extension interface.
	 */
	public static void registerExtensionInterface(
			MetaDataContext<Indexed, Indexed> context, Class<?> objectClass) {
		context.addManagedObjectExtensionInterface(objectClass,
				new ClassExtensionInterfaceFactory());
	}

	/*
	 * ================ ExtensionInterfaceFactory =======================
	 */

	@Override
	public Object createExtensionInterface(ManagedObject managedObject) {

		// Downcast to the class managed object
		ClassManagedObject classManagedObject = (ClassManagedObject) managedObject;

		// Return the object as the extension interface
		return classManagedObject.getObject();
	}
}