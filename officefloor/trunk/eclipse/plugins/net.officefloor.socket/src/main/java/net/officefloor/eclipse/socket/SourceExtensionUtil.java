/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class providing helper methods.
 *
 * @author Daniel Sagenschneider
 */
public class SourceExtensionUtil {

	/**
	 * Creates the display for the input {@link Property}.
	 *
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param page
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 */
	public static void createPropertyDisplay(String label, String name,
			String defaultValue, Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Obtain the properties
		PropertyList properties = context.getPropertyList();

		// Obtain the property
		Property item = properties.getProperty(name);
		if (item == null) {
			item = properties.addProperty(name);
		}
		final Property property = item;

		// Default the property value if blank
		String propertyValue = property.getValue();
		if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
			property.setValue(defaultValue);
		}

		// Provide the label
		new Label(page, SWT.NONE).setText(label);

		// Provide the text to specify value
		final Text text = new Text(page, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		text.setText(property.getValue());
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				property.setValue(text.getText());
				context.notifyPropertiesChanged();
			}
		});
	}

	/**
	 * All access via static methods.
	 */
	private SourceExtensionUtil() {
	}

}