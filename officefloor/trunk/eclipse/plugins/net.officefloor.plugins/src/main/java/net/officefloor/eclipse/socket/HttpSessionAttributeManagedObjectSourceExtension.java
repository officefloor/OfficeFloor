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
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeManagedObjectSource;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeManagedObjectSource.HttpSessionAttributeDependencies;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link HttpSessionAttributeManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<HttpSessionAttributeDependencies, None, HttpSessionAttributeManagedObjectSource> {

	/*
	 * ================== ManagedObjectSourceExtension ========================
	 */

	@Override
	public Class<HttpSessionAttributeManagedObjectSource> getManagedObjectSourceClass() {
		return HttpSessionAttributeManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "HTTP Session Object";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {
		// No properties necessary
		SourceExtensionUtil.informNoPropertiesRequired(page);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		// Return the name
		return "HttpSessionAttribute";
	}

}