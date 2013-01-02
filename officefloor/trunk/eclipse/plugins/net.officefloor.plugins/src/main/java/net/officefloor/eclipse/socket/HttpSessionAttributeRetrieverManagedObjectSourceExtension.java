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
package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeRetrieverManagedObjectSource;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeRetrieverManagedObjectSource.HttpSessionAttributeRetrieverDependencies;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link HttpSessionAttributeRetrieverManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeRetrieverManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<HttpSessionAttributeRetrieverDependencies, None, HttpSessionAttributeRetrieverManagedObjectSource>,
		ExtensionOpener {

	/*
	 * ================== ManagedObjectSourceExtension ========================
	 */

	@Override
	public Class<HttpSessionAttributeRetrieverManagedObjectSource> getManagedObjectSourceClass() {
		return HttpSessionAttributeRetrieverManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "HTTP Session Attribute Retriever";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {
		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil
				.createPropertyClass(
						"Object type",
						HttpSessionAttributeRetrieverManagedObjectSource.PROPERTY_TYPE_NAME,
						page, context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the object class
		String className = properties
				.getProperty(
						HttpSessionAttributeRetrieverManagedObjectSource.PROPERTY_TYPE_NAME)
				.getValue();
		int simpleNameIndex = className.lastIndexOf('.');
		if (simpleNameIndex >= 0) {
			// Strip to simple name (+1 to ignore '.')
			className = className.substring(simpleNameIndex + 1);
		}

		// Return the name
		return "HttpSessionObjectRetriever-" + className;
	}

	/*
	 * ====================== ExtensionOpener ==================================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the class
		String className = context
				.getPropertyList()
				.getPropertyValue(
						HttpSessionAttributeRetrieverManagedObjectSource.PROPERTY_TYPE_NAME,
						null);
		if (EclipseUtil.isBlank(className)) {
			throw new Exception("Class name not specified");
		}

		// Transform class name to resource name and open
		String resourceName = className.replace('.', '/') + ".class";
		context.openClasspathResource(resourceName);
	}

}