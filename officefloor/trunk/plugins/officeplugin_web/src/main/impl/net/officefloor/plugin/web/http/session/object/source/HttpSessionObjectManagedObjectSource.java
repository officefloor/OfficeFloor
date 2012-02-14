/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.session.object.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.object.HttpSessionObject;

/**
 * {@link ManagedObjectSource} to obtain an {@link HttpSessionObject} for an
 * Object in the {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies, None> {

	/**
	 * Dependency keys for the {@link HttpSessionObjectManagedObjectSource}.
	 */
	public static enum HttpSessionObjectDependencies {
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
	protected void loadMetaData(
			MetaDataContext<HttpSessionObjectDependencies, None> context)
			throws Exception {

		// Load the meta-data
		context.setManagedObjectClass(HttpSessionObjectManagedObject.class);
		context.setObjectClass(HttpSessionObject.class);
		context.addDependency(HttpSessionObjectDependencies.HTTP_SESSION,
				HttpSession.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionObjectManagedObject();
	}

}