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
package net.officefloor.eclipse.socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.eclipse.extension.managedfunctionsource.FunctionDocumentationContext;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.plugin.web.http.route.HttpRouteManagedFunctionSource;

/**
 * {@link ManagedFunctionSourceExtension} for the
 * {@link HttpRouteManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSourceExtension
		extends AbstractSocketManagedFunctionSourceExtension<HttpRouteManagedFunctionSource> {

	/**
	 * Initiate.
	 */
	public HttpRouteWorkSourceExtension() {
		super(HttpRouteManagedFunctionSource.class, "Http Route");
	}

	/*
	 * ====================== WorkSourceExtension =========================
	 */

	@Override
	public void createControl(Composite page, final ManagedFunctionSourceExtensionContext context) {

		// Properties
		SourceExtensionUtil.loadPropertyLayout(page);
		new Label(page, SWT.NONE).setText("Dynamically configured from Office meta-data");
	}

	@Override
	public String getFunctionDocumentation(FunctionDocumentationContext context) throws Throwable {

		// Should only be the one function

		// Create the routing list as documentation
		return "Route HTTP requests";
	}

}