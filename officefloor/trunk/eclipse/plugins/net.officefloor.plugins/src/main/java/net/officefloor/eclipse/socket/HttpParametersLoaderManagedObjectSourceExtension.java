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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderDependencies;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderManagedObjectSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link HttpParametersLoaderManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<HttpParametersLoaderDependencies, None, HttpParametersLoaderManagedObjectSource> {

	/*
	 * ================= ManagedObjectSourceExtension =========================
	 */

	@Override
	public Class<HttpParametersLoaderManagedObjectSource> getManagedObjectSourceClass() {
		return HttpParametersLoaderManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "HTTP Parameters Loader";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {

		// Properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Bean type",
				HttpParametersLoaderManagedObjectSource.PROPERTY_TYPE_NAME,
				page, context, null);
		SourceExtensionUtil
				.createPropertyCheckbox(
						"Case sensitive names",
						HttpParametersLoaderManagedObjectSource.PROPERTY_CASE_INSENSITIVE,
						true, Boolean.TRUE.toString(),
						Boolean.FALSE.toString(), page, context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {

		// Obtain the bean type
		String beanTypeName = properties.getProperty(
				HttpParametersLoaderWorkSource.PROPERTY_TYPE_NAME).getValue();
		int simpleNameIndex = beanTypeName.lastIndexOf('.');
		if (simpleNameIndex >= 0) {
			// Strip to simple name (+1 to ignore '.')
			beanTypeName = beanTypeName.substring(simpleNameIndex + 1);
		}

		// Return the name
		return "Http load " + beanTypeName;
	}

}