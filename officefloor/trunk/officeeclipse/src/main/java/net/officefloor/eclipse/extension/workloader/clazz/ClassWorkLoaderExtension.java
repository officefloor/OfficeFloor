/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.extension.workloader.clazz;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.InputFilter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.clazz.ClassWorkLoader;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkLoaderExtension} for the {@link ClassWorkLoader}.
 * 
 * @author Daniel
 */
public class ClassWorkLoaderExtension implements WorkLoaderExtension {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getWorkLoaderClass()
	 */
	@Override
	public Class<? extends WorkLoader> getWorkLoaderClass() {
		return ClassWorkLoader.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "Class";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * createControl(org.eclipse.swt.widgets.Composite,
	 * net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext)
	 */
	@Override
	public List<WorkLoaderProperty> createControl(Composite page,
			final WorkLoaderExtensionContext context) {

		// Specify layout
		page.setLayout(new GridLayout(2, false));

		// Provide the only property which is the class name
		final WorkLoaderProperty property = new WorkLoaderProperty(
				ClassWorkLoader.CLASS_NAME_PROPERTY_NAME);
		final List<WorkLoaderProperty> properties = new ArrayList<WorkLoaderProperty>(
				1);
		properties.add(property);

		// Provide listing of class names
		ClasspathFilter filter = new ClasspathFilter();
		filter.addJavaElementFilter(new InputFilter<IJavaElement>() {
			@Override
			public boolean isFilter(IJavaElement item) {
				return !(item instanceof ITypeRoot);
			}
		});
		new InputHandler<String>(page, new ClasspathSelectionInput(context
				.getProject(), filter), new InputListener() {

			@Override
			public void notifyValueChanged(Object value) {

				// Must be java element (due to filter)
				IJavaElement javaElement = (IJavaElement) value;

				// Obtain class name
				String className = ClasspathUtil.getClassName(javaElement);

				// Inform of change
				property.setValue(className);
				context.notifyPropertiesChanged(properties);
			}

			@Override
			public void notifyValueInvalid(String message) {
				context.setErrorMessage(message);
			}
		});

		// Return the properties
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getSuggestedWorkName(java.util.List)
	 */
	@Override
	public String getSuggestedWorkName(List<WorkLoaderProperty> properties) {

		// Find the property containing the class name
		WorkLoaderProperty classNameProperty = null;
		for (WorkLoaderProperty property : properties) {
			if (ClassWorkLoader.CLASS_NAME_PROPERTY_NAME.equals(property
					.getName())) {
				classNameProperty = property;
			}
		}

		// Ensure found property
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

}
