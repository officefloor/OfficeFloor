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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedfunctionsource.FunctionDocumentationContext;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.plugin.jndi.work.JndiWork;
import net.officefloor.plugin.jndi.work.JndiWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedFunctionSourceExtension} for the {@link JndiWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiWorkSourceExtension implements
		ManagedFunctionSourceExtension<JndiWork, JndiWorkSource>,
		ExtensionClasspathProvider, ExtensionOpener {

	/*
	 * ====================== WorkSourceExtension =====================
	 */

	@Override
	public Class<JndiWorkSource> getManagedFunctionSourceClass() {
		return JndiWorkSource.class;
	}

	@Override
	public String getManagedFunctionSourceLabel() {
		return "JNDI Work";
	}

	@Override
	public void createControl(Composite page, ManagedFunctionSourceExtensionContext context) {
		SourceExtensionUtil.loadPropertyLayout(page);

		// Add the JNDI Name
		SourceExtensionUtil.createPropertyText("JNDI Name",
				JndiWorkSource.PROPERTY_JNDI_NAME, null, page, context, null);

		// Add the Work Type
		SourceExtensionUtil.createPropertyClass("JNDI Object Type",
				JndiWorkSource.PROPERTY_WORK_TYPE, page, context, null);

		// Add the Facade
		SourceExtensionUtil.createPropertyClass("Facade Class Name",
				JndiWorkSource.PROPERTY_FACADE_CLASS, page, context, null);
	}

	@Override
	public String getSuggestedFunctionNamespaceName(PropertyList properties) {
		return this.getSimpleTypeName(JndiWorkSource.PROPERTY_WORK_TYPE,
				properties);
	}

	@Override
	public String getFunctionDocumentation(FunctionDocumentationContext context)
			throws Throwable {

		String methodName = context.getManagedFunctionName();
		PropertyList properties = context.getPropertyList();

		// Obtain the JNDI name
		String jndiName = properties.getPropertyValue(
				JndiWorkSource.PROPERTY_JNDI_NAME, null);

		// Obtain the work and facade simple types
		String workTypeName = this.getSimpleTypeName(
				JndiWorkSource.PROPERTY_WORK_TYPE, properties);
		String facadeClassName = this.getSimpleTypeName(
				JndiWorkSource.PROPERTY_FACADE_CLASS, properties);

		// Return based on whether using facade
		if (facadeClassName != null) {
			return "Invoking facade method " + facadeClassName + "."
					+ methodName + "(...) on JNDI Object " + jndiName
					+ " of type " + workTypeName;
		} else {
			return "Invoking method " + workTypeName + "." + methodName
					+ "(...) on JNDI Object " + jndiName;
		}
	}

	/**
	 * Obtains the simple type name.
	 * 
	 * @param propertyName
	 *            Name of {@link Property} containing the type.
	 * @param properties
	 *            {@link PropertyList}.
	 * @return Simple type name or <code>null</code> if could not obtain.
	 */
	private String getSimpleTypeName(String propertyName,
			PropertyList properties) {

		// Obtain the type property
		Property typeProperty = properties.getProperty(propertyName);
		if (typeProperty == null) {
			return null; // No type
		}

		// Ensure have type
		String typeName = typeProperty.getValue();
		if (EclipseUtil.isBlank(typeName)) {
			return null;
		}

		// Obtain name (minus package)
		String[] fragments = typeName.split("\\.");
		String simpleTypeName = fragments[fragments.length - 1];

		// Return the simple type name
		return simpleTypeName;
	}

	/*
	 * ======================= ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				JndiWorkSource.class) };
	}

	/*
	 * ========================= ExtensionOpener ==============================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		PropertyList properties = context.getPropertyList();

		// Attempt to open facade before object type.
		// (Can navigate to object type from facade)
		String objectTypeName = properties.getPropertyValue(
				JndiWorkSource.PROPERTY_FACADE_CLASS, null);
		if (EclipseUtil.isBlank(objectTypeName)) {
			objectTypeName = properties.getPropertyValue(
					JndiWorkSource.PROPERTY_WORK_TYPE, null);
		}

		// Ensure have object type
		if (EclipseUtil.isBlank(objectTypeName)) {
			throw new Exception("No work type provided");
		}

		// Translate object type name to resource name
		String resourceName = objectTypeName.replace('.', '/') + ".class";

		// Open the object type file
		context.openClasspathResource(resourceName);
	}

}