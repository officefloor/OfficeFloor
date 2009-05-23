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
package net.officefloor.eclipse.extension.managedobjectsource.xml;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.xml.unmarshall.flat.FlatXmlUnmarshallerManagedObjectSource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for
 * {@link FlatXmlUnmarshallerManagedObjectSource}.
 * 
 * @author Daniel
 */
public class FlatXmlUnmarshallerManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, None, FlatXmlUnmarshallerManagedObjectSource> {

	/*
	 * ================ ManagedObjectSourceExtension =========================
	 */

	@Override
	public Class<FlatXmlUnmarshallerManagedObjectSource> getManagedObjectSourceClass() {
		return FlatXmlUnmarshallerManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "XML Unmarshaller (flat)";
	}

	@Override
	public void createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Obtain the class name property
		Property property = context.getPropertyList().getProperty(
				FlatXmlUnmarshallerManagedObjectSource.CLASS_PROPERTY_NAME);
		if (property == null) {
			property = context.getPropertyList().addProperty(
					FlatXmlUnmarshallerManagedObjectSource.CLASS_PROPERTY_NAME);
		}
		final Property classNameProperty = property;

		// Provide listing of class names
		ClasspathFilter filter = new ClasspathFilter();
		filter.addJavaClassFilter();
		new InputHandler<String>(page, new ClasspathSelectionInput(context
				.getProject(), filter), new InputListener() {

			@Override
			public void notifyValueChanged(Object value) {

				// Must be java element (due to filter)
				IJavaElement javaElement = (IJavaElement) value;

				// Obtain the class name
				String className = ClasspathUtil.getClassName(javaElement);

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
				.getProperty(FlatXmlUnmarshallerManagedObjectSource.CLASS_PROPERTY_NAME);
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

		// Return the simple class name with unmashaller suffix
		return simpleClassName + "Unmarshaller";
	}

}