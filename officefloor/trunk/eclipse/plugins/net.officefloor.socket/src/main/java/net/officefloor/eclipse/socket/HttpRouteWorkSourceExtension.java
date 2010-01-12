/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.route.source.HttpRouteTask;
import net.officefloor.plugin.socket.server.http.route.source.HttpRouteWorkSource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSourceExtension extends
		AbstractSocketWorkSourceExtension<HttpRouteTask, HttpRouteWorkSource> {

	/**
	 * Initiate.
	 */
	public HttpRouteWorkSourceExtension() {
		super(HttpRouteWorkSource.class, "Http Route");
	}

	/*
	 * ====================== WorkSourceExtension =========================
	 */

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
							.startsWith(HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX))) {
				continue; // not a routing property
			}
			name = name.substring(HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX
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
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Should only be the one task

		// Create the routing list as documentation
		StringBuilder doc = new StringBuilder();
		doc.append("Routes based on the ");
		doc.append(HttpRequest.class.getSimpleName());
		doc.append(" request URI to the flow as follows:\n");

		// Provide listing of routes (in order)
		for (Property property : context.getPropertyList()) {
			String name = property.getName();
			if ((name == null)
					|| (!name
							.startsWith(HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX))) {
				continue; // not a routing property
			}
			name = name.substring(HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX
					.length());

			// Document the routing entry
			doc.append("\n\t");
			doc.append(name);
			doc.append("  [");
			doc.append(property.getValue());
			doc.append("]");
		}

		// Add the default route
		doc.append("\n\tdefault  (no match)");

		// Return the documentation
		return doc.toString();
	}

	/**
	 * Entry in routing.
	 */
	public static class RoutingEntry {

		/**
		 * Name identifying this {@link RoutingEntry}.
		 */
		private String name;

		/**
		 * Pattern for matching to use this {@link RoutingEntry}.
		 */
		private String pattern;

		/**
		 * Default construction for use in {@link BeanDialog}.
		 */
		public RoutingEntry() {
		}

		/**
		 * Initialise.
		 * 
		 * @param name
		 *            Name.
		 * @param pattern
		 *            Pattern.
		 */
		public RoutingEntry(String name, String pattern) {
			this.name = (name == null ? "" : name);
			this.pattern = (pattern == null ? "" : pattern);
		}

		/**
		 * Obtains the name identifying this {@link RoutingEntry}.
		 * 
		 * @return Name.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Specifies the name.
		 * 
		 * @param name
		 *            Name.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Obtains the pattern.
		 * 
		 * @return Pattern.
		 */
		public String getPattern() {
			return this.pattern;
		}

		/**
		 * Specifies the pattern.
		 * 
		 * @param pattern
		 *            Pattern.
		 */
		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		/**
		 * Loads the {@link Property} for this {@link RoutingEntry} to the
		 * {@link PropertyList}.
		 * 
		 * @param {@link PropertyList}.
		 */
		public void loadProperty(PropertyList propertyList) {

			// Only provide prefix if name provided
			String propertyName = ((this.name == null) || this.name.trim()
					.length() == 0) ? ""
					: (HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX + this.name);

			// Load the property
			Property property = propertyList.addProperty(propertyName);
			property.setValue(this.pattern);
		}

	}

}