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
package net.officefloor.eclipse.extension.worksource.clazz;

import java.lang.reflect.Method;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link ClassWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkSourceExtension implements
		WorkSourceExtension<ClassWork, ClassWorkSource>,
		ExtensionClasspathProvider, ExtensionOpener {

	/*
	 * =================== WorkSourceExtension ==========================
	 */

	@Override
	public Class<ClassWorkSource> getWorkSourceClass() {
		return ClassWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "Class";
	}

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Provide property for class name
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Class",
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, page, context, null);
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {

		// Obtain the class name property
		Property classNameProperty = properties
				.getProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME);
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
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Obtain the name of the class
		String className = context.getPropertyList().getPropertyValue(
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				"<class not specified>");

		// Obtain the task name (also the method name)
		String taskName = context.getTaskName();

		// Attempt to obtain the method signature
		String methodSignature = null;
		try {
			// Obtain the method
			Class<?> clazz = context.getClassLoader().loadClass(className);
			Method method = null;
			for (Method possibleMethod : clazz.getMethods()) {
				if (possibleMethod.getName().equals(taskName)) {
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
			return "Invokes method:\n\n\t" + className + "." + taskName
					+ "(...)";
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
		return new ClasspathProvision[] { new TypeClasspathProvision(
				ClassWorkSource.class) };
	}

	/*
	 * ========================= ExtensionOpener ==============================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the name of the class
		String className = context.getPropertyList().getPropertyValue(
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, null);

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