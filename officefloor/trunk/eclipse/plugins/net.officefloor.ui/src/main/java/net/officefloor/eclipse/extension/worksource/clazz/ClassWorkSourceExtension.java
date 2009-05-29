/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.extension.worksource.clazz;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link ClassWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkSourceExtension implements
		WorkSourceExtension<ClassWork, ClassWorkSource>,
		ExtensionClasspathProvider {

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

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Obtain the class name property
		Property property = context.getPropertyList().getProperty(
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME);
		if (property == null) {
			property = context.getPropertyList().addProperty(
					ClassWorkSource.CLASS_NAME_PROPERTY_NAME);
		}
		final Property classNameProperty = property;

		// Provide listing of class names
		new InputHandler<String>(page, new ClasspathSelectionInput(context
				.getProject()), new InputListener() {

			@Override
			public void notifyValueChanged(Object value) {

				// Obtain the class name
				String className = (value == null ? null : value.toString());

				// Inform of change of class name
				classNameProperty.setValue(className);
				context.notifyPropertiesChanged();
			}

			@Override
			public void notifyValueInvalid(String message) {
				context.setErrorMessage(message);
			}
		});
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

	/*
	 * ======================= ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				ClassWorkSource.class) };
	}

}