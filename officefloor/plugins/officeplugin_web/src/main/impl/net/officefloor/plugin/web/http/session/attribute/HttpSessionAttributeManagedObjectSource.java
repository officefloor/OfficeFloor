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
package net.officefloor.plugin.web.http.session.attribute;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObjectSource} to obtain an {@link HttpSessionAttribute} for an
 * Object in the {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeManagedObjectSource extends
		AbstractManagedObjectSource<HttpSessionAttributeManagedObjectSource.HttpSessionAttributeDependencies, None> {

	/**
	 * Dependency keys for the {@link HttpSessionAttributeManagedObjectSource}.
	 */
	public static enum HttpSessionAttributeDependencies {
		HTTP_SESSION
	}

	/*
	 * ==================== AbstractManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpSessionAttributeDependencies, None> context) throws Exception {

		// Load the meta-data
		context.setManagedObjectClass(HttpSessionAttributeManagedObject.class);
		context.setObjectClass(HttpSessionAttribute.class);
		context.addDependency(HttpSessionAttributeDependencies.HTTP_SESSION, HttpSession.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionAttributeManagedObject();
	}

}