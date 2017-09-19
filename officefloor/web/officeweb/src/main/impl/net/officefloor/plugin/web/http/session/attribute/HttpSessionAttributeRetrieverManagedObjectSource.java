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
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} that retrieves the {@link Object} from the
 * {@link HttpSessionAttribute}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeRetrieverManagedObjectSource extends
		AbstractManagedObjectSource<HttpSessionAttributeRetrieverManagedObjectSource.HttpSessionAttributeRetrieverDependencies, None> {

	/**
	 * Property to specify the {@link Class} name of the {@link Object}.
	 */
	public static final String PROPERTY_TYPE_NAME = "type.name";

	/**
	 * Dependency keys for the
	 * {@link HttpSessionAttributeRetrieverManagedObjectSource}.
	 */
	public static enum HttpSessionAttributeRetrieverDependencies {
		HTTP_SESSION_OBJECT
	}

	/*
	 * ================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TYPE_NAME);
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpSessionAttributeRetrieverDependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the type for being returned
		String typeName = mosContext.getProperty(PROPERTY_TYPE_NAME);
		Class<?> type = mosContext.loadClass(typeName);

		// Load the meta-data
		context.setManagedObjectClass(HttpSessionAttributeRetrieverManagedObject.class);
		context.setObjectClass(type);
		context.addDependency(HttpSessionAttributeRetrieverDependencies.HTTP_SESSION_OBJECT,
				HttpSessionAttribute.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionAttributeRetrieverManagedObject();
	}

}