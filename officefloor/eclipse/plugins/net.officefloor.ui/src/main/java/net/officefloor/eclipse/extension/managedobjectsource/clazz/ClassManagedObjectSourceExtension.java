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
package net.officefloor.eclipse.extension.managedobjectsource.clazz;

import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * {@link ManagedObjectSourceExtension} for {@link ClassManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSourceExtension
		implements ManagedObjectSourceExtension<Indexed, Indexed, ClassManagedObjectSource>, ExtensionOpener {

	/*
	 * ================ ManagedObjectSourceExtension =========================
	 */

	@Override
	public Class<ClassManagedObjectSource> getManagedObjectSourceClass() {
		return ClassManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "Class";
	}

	@Override
	public void createControl(Composite page, final ManagedObjectSourceExtensionContext context) {

		// Provide property for class name
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Class", ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, page,
				context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the class name property
		Property classNameProperty = properties.getProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME);
		if (classNameProperty == null) {
			// No suggestion as no class name
			return null;
		}

		// Ensure have class name
		String className = classNameProperty.getValue();
		if (EclipseUtil.isBlank(className)) {
			return null;
		}

		// Obtain name (minus package)
		String[] fragments = className.split("\\.");
		String simpleClassName = fragments[fragments.length - 1];

		// Return the simple class name
		return simpleClassName;
	}

	/*
	 * ========================= ExtensionOpener ==============================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the name of the class
		String className = context.getPropertyList().getPropertyValue(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				null);

		// Ensure have class name
		if (EclipseUtil.isBlank(className)) {
			throw new Exception("No class name provided");
		}

		// Translate class name to resource name
		String resourceName = className.replace('.', '/') + ".class";

		// Open the class file
		context.openClasspathResource(resourceName);
	}

}