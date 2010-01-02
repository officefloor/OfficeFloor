/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.session.object.source.HttpSessionObjectManagedObjectSource;
import net.officefloor.plugin.socket.server.http.session.object.source.HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link HttpSessionObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<HttpSessionObjectDependencies, None, HttpSessionObjectManagedObjectSource>,
		ExtensionOpener {

	/*
	 * ================== ManagedObjectSourceExtension ========================
	 */

	@Override
	public Class<HttpSessionObjectManagedObjectSource> getManagedObjectSourceClass() {
		return HttpSessionObjectManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "HTTP Session Object";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {
		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Object class",
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, page,
				context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the object class
		String className = properties.getProperty(
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME)
				.getValue();
		int simpleNameIndex = className.lastIndexOf('.');
		if (simpleNameIndex >= 0) {
			// Strip to simple name (+1 to ignore '.')
			className = className.substring(simpleNameIndex + 1);
		}

		// Return the name
		return "Http Session Object " + className;
	}

	/*
	 * ====================== ExtensionOpener ==================================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the class
		String className = context.getPropertyList().getPropertyValue(
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, null);
		if (EclipseUtil.isBlank(className)) {
			throw new Exception("Class name not specified");
		}

		// Transform class name to resource name and open
		String resourceName = className.replace('.', '/') + ".class";
		context.openClasspathResource(resourceName);
	}

}