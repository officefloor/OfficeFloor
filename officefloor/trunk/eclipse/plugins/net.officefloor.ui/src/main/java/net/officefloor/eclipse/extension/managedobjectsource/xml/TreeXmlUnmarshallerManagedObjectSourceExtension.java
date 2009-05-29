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
package net.officefloor.eclipse.extension.managedobjectsource.xml;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerManagedObjectSource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for
 * {@link TreeXmlUnmarshallerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshallerManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, None, TreeXmlUnmarshallerManagedObjectSource> {

	/*
	 * ================ ManagedObjectSourceExtension =========================
	 */

	@Override
	public Class<TreeXmlUnmarshallerManagedObjectSource> getManagedObjectSourceClass() {
		return TreeXmlUnmarshallerManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "XML Unmarshaller";
	}

	@Override
	public void createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Obtain the configuration property
		Property property = context
				.getPropertyList()
				.getProperty(
						TreeXmlUnmarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME);
		if (property == null) {
			property = context
					.getPropertyList()
					.addProperty(
							TreeXmlUnmarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME);
		}
		final Property configurationProperty = property;

		// Provide listing of class names
		new InputHandler<String>(page, new ClasspathSelectionInput(context
				.getProject()), new InputListener() {

			@Override
			public void notifyValueChanged(Object value) {

				// Obtain the resource location on class path
				String resourceLocation = (value == null ? null : value
						.toString());

				// Inform of change of resource
				configurationProperty.setValue(resourceLocation);
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
		return "XmlUnmarshaller";
	}

}