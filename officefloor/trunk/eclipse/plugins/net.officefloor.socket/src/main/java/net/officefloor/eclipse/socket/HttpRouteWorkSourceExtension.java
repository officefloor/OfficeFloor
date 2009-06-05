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

import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.work.http.route.HttpRouteTask;
import net.officefloor.plugin.work.http.route.HttpRouteWorkSource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpRouteWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSourceExtension implements
		WorkSourceExtension<HttpRouteTask, HttpRouteWorkSource>,
		ExtensionClasspathProvider {

	/*
	 * ====================== WorkLoaderExtension =========================
	 */

	@Override
	public Class<HttpRouteWorkSource> getWorkSourceClass() {
		return HttpRouteWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP Route";
	}

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Specify layout of page
		page.setLayout(new GridLayout(1, false));

		// Provide listing of routes
		BeanListInput<RoutingEntry> input = new BeanListInput<RoutingEntry>(
				RoutingEntry.class);
		input.addProperty("name", 1);
		input.addProperty("pattern", 2);

		// Add the initial routing entries
		for (Property property : context.getPropertyList()) {
			String name = property.getName();
			if ((name == null)
					|| (!name
							.startsWith(HttpRouteWorkSource.ROUTE_PROPERTY_PREFIX))) {
				continue; // not a routing property
			}
			name = name.substring(HttpRouteWorkSource.ROUTE_PROPERTY_PREFIX
					.length());

			// Add the initial routing entry
			input.addBean(new RoutingEntry(name, property.getValue()));
		}

		// Add control to alter properties
		new InputHandler<List<RoutingEntry>>(page, input, new InputListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void notifyValueChanged(Object value) {
				List<RoutingEntry> routingEntries = (List<RoutingEntry>) value;

				// Create the listing of properties
				PropertyList properties = context.getPropertyList();
				properties.clear();
				for (RoutingEntry routingEntry : routingEntries) {
					routingEntry.loadProperty(properties);
				}

				// Notify of change
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
		return "HttpRouter";
	}

	/*
	 * =================== ExtensionClasspathProvider =====================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				HttpRouteWorkSource.class) };
	}

}