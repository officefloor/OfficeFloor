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
package net.officefloor.eclipse.extension.managedobjectsource.clazz;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for {@link ClassManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<Indexed, Indexed, ClassManagedObjectSource> {

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
	public void createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Obtain the class name property
		Property property = context.getPropertyList().getProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME);
		if (property == null) {
			property = context.getPropertyList().addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME);
		}
		final Property classNameProperty = property;

		// Provide listing of class names
		new InputHandler<String>(page, new ClasspathClassInput(context
				.getProject(), page.getShell()), new InputListener() {

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
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the class name property
		Property classNameProperty = properties
				.getProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME);
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

}