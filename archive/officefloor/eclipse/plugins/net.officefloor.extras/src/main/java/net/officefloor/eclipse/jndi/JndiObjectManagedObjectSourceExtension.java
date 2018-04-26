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
package net.officefloor.eclipse.jndi;

import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.jndi.object.JndiObjectManagedObjectSource;
import net.officefloor.plugin.jndi.object.JndiObjectManagedObjectSource.JndiObjectDependency;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link JndiObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiObjectManagedObjectSourceExtension implements
		ManagedObjectSourceExtension<JndiObjectDependency, None, JndiObjectManagedObjectSource>, ExtensionOpener {

	/*
	 * ====================== ManagedObjectSourceExtension =====================
	 */

	@Override
	public Class<JndiObjectManagedObjectSource> getManagedObjectSourceClass() {
		return JndiObjectManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "JNDI Object";
	}

	@Override
	public void createControl(Composite page, ManagedObjectSourceExtensionContext context) {

		// Specify layout
		SourceExtensionUtil.loadPropertyLayout(page);

		// Provide JNDI name
		SourceExtensionUtil.createPropertyText("JNDI Name", JndiObjectManagedObjectSource.PROPERTY_JNDI_NAME, null,
				page, context, null);

		// Provide object type
		SourceExtensionUtil.createPropertyClass("Object Type", JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE, page,
				context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the object type property
		Property objectTypeProperty = properties.getProperty(JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE);
		if (objectTypeProperty == null) {
			// No suggestion as no object type
			return null;
		}

		// Ensure have object type
		String className = objectTypeProperty.getValue();
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

		// Obtain the name of the object type
		String objectTypeName = context.getPropertyList()
				.getPropertyValue(JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE, null);

		// Ensure have object type
		if (EclipseUtil.isBlank(objectTypeName)) {
			throw new Exception("No object type provided");
		}

		// Translate object type name to resource name
		String resourceName = objectTypeName.replace('.', '/') + ".class";

		// Open the object type file
		context.openClasspathResource(resourceName);
	}

}