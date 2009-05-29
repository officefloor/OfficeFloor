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
package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpServer.HttpServerFlows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, HttpServerFlows, HttpServerSocketManagedObjectSource>,
		ExtensionClasspathProvider {

	/*
	 * ================= ManagedObjectSourceExtension ========================
	 */

	@Override
	public Class<HttpServerSocketManagedObjectSource> getManagedObjectSourceClass() {
		return HttpServerSocketManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "HTTP";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {

		// Specify layout of page
		page.setLayout(new GridLayout(2, false));

		// Provide the properties
		this.createPropertyDisplay("Port: ",
				HttpServerSocketManagedObjectSource.PROPERTY_PORT, "80", page,
				context);
		this.createPropertyDisplay("Buffer size: ",
				HttpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
				"1024", page, context);
		this.createPropertyDisplay("Recommended segments per message: ",
				HttpServerSocketManagedObjectSource.PROPERTY_MESSAGE_SIZE,
				"10", page, context);
		this
				.createPropertyDisplay(
						"Maximum connextions per listener: ",
						HttpServerSocketManagedObjectSource.PROPERTY_MAXIMUM_CONNECTIONS,
						"64", page, context);
	}

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
	private void createPropertyDisplay(String label, String name,
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

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "HTTP";
	}

	/*
	 * ========================== ExtensionClasspathProvider =================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				HttpServerSocketManagedObjectSource.class) };
	}

}