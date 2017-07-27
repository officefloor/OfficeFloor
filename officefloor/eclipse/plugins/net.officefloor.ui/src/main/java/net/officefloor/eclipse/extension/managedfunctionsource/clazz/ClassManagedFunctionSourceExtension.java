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
package net.officefloor.eclipse.extension.managedfunctionsource.clazz;

import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Composite;

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
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * {@link ManagedFunctionSourceExtension} for the
 * {@link ClassManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSourceExtension implements ManagedFunctionSourceExtension<ClassManagedFunctionSource>,
		ExtensionClasspathProvider, ExtensionOpener {

	/*
	 * ================= ManagedFunctionSourceExtension =================
	 */

	@Override
	public Class<ClassManagedFunctionSource> getManagedFunctionSourceClass() {
		return ClassManagedFunctionSource.class;
	}

	@Override
	public String getManagedFunctionSourceLabel() {
		return "Class";
	}

	@Override
	public void createControl(Composite page, final ManagedFunctionSourceExtensionContext context) {

		// Provide property for class name
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Class", ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, page,
				context, null);
	}

	@Override
	public String getSuggestedFunctionNamespaceName(PropertyList properties) {

		// Obtain the class name property
		Property classNameProperty = properties.getProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME);
		if (classNameProperty == null) {
			// No suggestion as no class name
			return null;
		}

		// Ensure have class name
		String className = classNameProperty.getValue();
		if ((className == null) || (className.trim().length() == 0)) {
			return null;
		}

		// Obtain name (minus package)
		String[] fragments = className.split("\\.");
		String simpleClassName = fragments[fragments.length - 1];

		// Return the simple class name
		return simpleClassName;
	}

	@Override
	public String getFunctionDocumentation(FunctionDocumentationContext context) throws Throwable {

		// Obtain the name of the class
		String className = context.getPropertyList()
				.getPropertyValue(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, "<class not specified>");

		// Obtain the function name (also the method name)
		String functionName = context.getManagedFunctionName();

		// Attempt to obtain the method signature
		String methodSignature = null;
		try {
			// Obtain the method
			Class<?> clazz = context.getClassLoader().loadClass(className);
			Method method = null;
			for (Method possibleMethod : clazz.getMethods()) {
				if (possibleMethod.getName().equals(functionName)) {
					method = possibleMethod; // method found
				}
			}
			if (method != null) {
				// Provide detailed method signature
				methodSignature = method.toGenericString();
			}

		} catch (Throwable ex) {
			// Ignore failure and set to have basic method signature
			methodSignature = null;
		}

		// Provide documentation
		if (EclipseUtil.isBlank(methodSignature)) {
			// Provide simple method (as could not obtain method signature)
			return "Invokes method:\n\n\t" + className + "." + functionName + "(...)";
		} else {
			// Provide detailed method signature
			return "Invokes method:\n\n\t" + methodSignature;
		}
	}

	/*
	 * ======================= ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(ClassManagedFunctionSource.class) };
	}

	/*
	 * ========================= ExtensionOpener ==============================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the name of the class
		String className = context.getPropertyList()
				.getPropertyValue(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, null);

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