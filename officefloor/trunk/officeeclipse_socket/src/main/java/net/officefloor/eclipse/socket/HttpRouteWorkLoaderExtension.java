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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.http.route.HttpRouteWorkLoader;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Http Route {@link WorkLoaderExtension}.
 * 
 * @author Daniel
 */
public class HttpRouteWorkLoaderExtension implements WorkLoaderExtension,
		ExtensionClasspathProvider {

	/*
	 * ====================== WorkLoaderExtension =========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getWorkLoaderClass()
	 */
	@Override
	public Class<? extends WorkLoader> getWorkLoaderClass() {
		return HttpRouteWorkLoader.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "HTTP Route";
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

		// Specify layout of page
		page.setLayout(new GridLayout(1, false));

		// Provide listing of routes
		BeanListInput<RoutingEntry> input = new BeanListInput<RoutingEntry>(
				RoutingEntry.class);
		input.addProperty("name", 1);
		input.addProperty("pattern", 2);
		new InputHandler<List<RoutingEntry>>(page, input, new InputListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void notifyValueChanged(Object value) {
				List<RoutingEntry> routingEntries = (List<RoutingEntry>) value;

				// Create the listing of properties
				List<WorkLoaderProperty> workLoaderProperties = new LinkedList<WorkLoaderProperty>();
				for (RoutingEntry routingEntry : routingEntries) {
					workLoaderProperties.add(routingEntry
							.createWorkLoaderProperty());
				}

				// Notify of change
				context.notifyPropertiesChanged(workLoaderProperties);
			}

			@Override
			public void notifyValueInvalid(String message) {
				context.setErrorMessage(message);
			}
		});

		// Initially no routing entries
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getSuggestedWorkName(java.util.List)
	 */
	@Override
	public String getSuggestedWorkName(List<WorkLoaderProperty> properties) {
		return "HttpRouter";
	}

	/*
	 * =================== ExtensionClasspathProvider =====================
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
				HttpRouteWorkLoader.class) };
	}

}
