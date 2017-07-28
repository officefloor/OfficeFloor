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

import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedfunctionsource.FunctionDocumentationContext;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderManagedFunctionSource;

/**
 * {@link ManagedFunctionSourceExtension} for the
 * {@link HttpParametersLoaderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedFunctionSourceExtension
		extends AbstractSocketManagedFunctionSourceExtension<HttpParametersLoaderManagedFunctionSource> {

	/**
	 * Initiate.
	 */
	public HttpParametersLoaderManagedFunctionSourceExtension() {
		super(HttpParametersLoaderManagedFunctionSource.class, "Http Parameters Loader");
	}

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public Class<HttpParametersLoaderManagedFunctionSource> getManagedFunctionSourceClass() {
		return HttpParametersLoaderManagedFunctionSource.class;
	}

	@Override
	public void createControl(Composite page, ManagedFunctionSourceExtensionContext context) {

		// Properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Bean type",
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME, page, context, null);
		SourceExtensionUtil.createPropertyCheckbox("Case sensitive names",
				HttpParametersLoaderManagedFunctionSource.PROPERTY_CASE_INSENSITIVE, true, Boolean.TRUE.toString(),
				Boolean.FALSE.toString(), page, context, null);
	}

	@Override
	public String getSuggestedFunctionNamespaceName(PropertyList properties) {

		// Obtain the bean type
		String beanTypeName = properties.getProperty(HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME)
				.getValue();
		int simpleNameIndex = beanTypeName.lastIndexOf('.');
		if (simpleNameIndex >= 0) {
			// Strip to simple name (+1 to ignore '.')
			beanTypeName = beanTypeName.substring(simpleNameIndex + 1);
		}

		// Return the name
		return "Http load " + beanTypeName;
	}

	@Override
	public String getFunctionDocumentation(FunctionDocumentationContext context) throws Throwable {

		// Should always have the one function

		// Obtain the object type
		String objectType = context.getPropertyList().getPropertyValue(
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME, "<object type not specified>");

		// Return documentation
		return "Loads HTTP parameter values of the " + HttpRequest.class.getSimpleName() + " onto the object ("
				+ objectType + ") via the corresponding setXxx(...) methods.";
	}

}