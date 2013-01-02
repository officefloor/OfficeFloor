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
package net.officefloor.eclipse.extension.managedobjectsource.xml;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshallerManagedObjectSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link ManagedObjectSourceExtension} for
 * {@link TreeXmlMarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshallerManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, None, TreeXmlMarshallerManagedObjectSource> {

	/*
	 * ================ ManagedObjectSourceExtension =========================
	 */

	@Override
	public Class<TreeXmlMarshallerManagedObjectSource> getManagedObjectSourceClass() {
		return TreeXmlMarshallerManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "XML Marshaller";
	}

	@Override
	public void createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Specify layout
		page.setLayout(new GridLayout(2, false));

		// Obtain the configuration property
		Property property = context
				.getPropertyList()
				.getProperty(
						TreeXmlMarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME);
		if (property == null) {
			property = context
					.getPropertyList()
					.addProperty(
							TreeXmlMarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME);
		}
		final Property configurationProperty = property;

		// Provide listing of class names
		new Label(page, SWT.NONE).setText("Configuration: ");
		InputHandler<String> fileName = new InputHandler<String>(page,
				new ClasspathFileInput(context.getProject(), page.getShell()),
				new InputListener() {

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
		fileName.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "XmlMarshaller";
	}

}