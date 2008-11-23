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
package net.officefloor.eclipse.socket;

import java.util.Arrays;
import java.util.List;

import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpServerSocketManagedObjectSource;

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
 * HTTP {@link ManagedObjectSourceExtension}.
 * 
 * @author Daniel
 */
public class HttpManagedObjectSourceExtension implements
		ManagedObjectSourceExtension, ExtensionClasspathProvider {

	/**
	 * {@link InitiateProperty} for the
	 * {@link ServerSocketManagedObjectSource#PROPERTY_PORT}.
	 */
	private final InitiateProperty portProperty = new InitiateProperty(
			HttpServerSocketManagedObjectSource.PROPERTY_PORT, "80");

	/**
	 * {@link InitiateProperty} for the
	 * {@link ServerSocketManagedObjectSource#PROPERTY_BUFFER_SIZE}.
	 */
	private final InitiateProperty bufferSizeProperty = new InitiateProperty(
			HttpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE, "1024");

	/**
	 * {@link InitiateProperty} for the
	 * {@link ServerSocketManagedObjectSource#PROPERTY_MESSAGE_SIZE}.
	 */
	private final InitiateProperty messageSizeProperty = new InitiateProperty(
			HttpServerSocketManagedObjectSource.PROPERTY_MESSAGE_SIZE, "10");

	/**
	 * {@link InitiateProperty} for the
	 * {@link ServerSocketManagedObjectSource#PROPERTY_MAXIMUM_CONNECTIONS}.
	 */
	private final InitiateProperty maxConnectionsProperty = new InitiateProperty(
			HttpServerSocketManagedObjectSource.PROPERTY_MAXIMUM_CONNECTIONS,
			"64");

	/**
	 * {@link InitiateProperty} instances.
	 */
	private final List<InitiateProperty> properties = Arrays.asList(
			this.portProperty, this.bufferSizeProperty,
			this.messageSizeProperty, this.maxConnectionsProperty);

	/*
	 * ================= ManagedObjectSourceExtension ========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getManagedObjectSourceClass()
	 */
	@Override
	public Class<? extends ManagedObjectSource<?, ?>> getManagedObjectSourceClass() {
		return HttpServerSocketManagedObjectSource.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#isUsable()
	 */
	@Override
	public boolean isUsable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "HTTP";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * net.officefloor.eclipse.
	 * extension.managedobjectsource.ManagedObjectSourceExtensionContext)
	 */
	@Override
	public List<InitiateProperty> createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Specify layout of page
		page.setLayout(new GridLayout(2, false));

		// Provide the properties
		this.createPropertyDisplay("Port: ", this.portProperty, page, context);
		this.createPropertyDisplay("Buffer size: ", this.bufferSizeProperty,
				page, context);
		this.createPropertyDisplay("Recommended segments per message: ",
				this.messageSizeProperty, page, context);
		this.createPropertyDisplay("Maximum connextions per listener: ",
				this.maxConnectionsProperty, page, context);

		// Return the properties
		return this.properties;
	}

	/**
	 * Creates the display for the input {@link InitiateProperty}.
	 * 
	 * @param label
	 *            Label for the property.
	 * @param property
	 *            {@link InitiateProperty}.
	 * @param page
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 */
	private void createPropertyDisplay(String label,
			final InitiateProperty property, Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Provide the label
		new Label(page, SWT.NONE).setText(label);

		// Provide the text to specify value
		final Text text = new Text(page, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		text.setText(property.getValue());
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				property.setValue(text.getText());
				context
						.notifyPropertiesChanged(HttpManagedObjectSourceExtension.this.properties);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #getSuggestedManagedObjectSourceName(java.util.List)
	 */
	@Override
	public String getSuggestedManagedObjectSourceName(
			List<InitiateProperty> properties) {
		return "HTTP";
	}

	/*
	 * ========================== ExtensionClasspathProvider =================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider
	 * #getClasspathProvisions()
	 */
	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				HttpServerSocketManagedObjectSource.class) };
	}

}
