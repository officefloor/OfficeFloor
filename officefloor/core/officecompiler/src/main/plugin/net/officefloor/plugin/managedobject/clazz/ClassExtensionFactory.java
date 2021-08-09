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

package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;

/**
 * <p>
 * {@link ExtensionFactory} that return the object of the
 * {@link ClassManagedObject}.
 * <p>
 * This allows any implemented interfaces of the class to be an extension
 * interface for the {@link ManagedObject} with implementation delegated to the
 * object instantiated from the class.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ClassExtensionFactory implements ExtensionFactory {

	/**
	 * Registers the extension.
	 * 
	 * @param context
	 *            {@link MetaDataContext} to add the extension interface.
	 * @param objectClass
	 *            Object class which is the extension interface.
	 */
	public static void registerExtension(MetaDataContext<Indexed, Indexed> context, Class<?> objectClass) {
		context.addManagedObjectExtension(objectClass, new ClassExtensionFactory());
	}

	/*
	 * ================ ExtensionFactory =======================
	 */

	@Override
	public Object createExtension(ManagedObject managedObject) {

		// Downcast to the class managed object
		ClassManagedObject classManagedObject = (ClassManagedObject) managedObject;

		// Return the object as the extension interface
		return classManagedObject.getObject();
	}
}
