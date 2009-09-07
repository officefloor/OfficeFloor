/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.session.object.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * <p>
 * {@link ManagedObjectSource} to obtain an Object from the {@link HttpSession}.
 * <p>
 * Should the Object not be available within the {@link HttpSession}, it is
 * instantiated and registered with the {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies, None> {

	/**
	 * Property to specify the {@link Class} name of the {@link Object}.
	 */
	public static final String PROPERTY_CLASS_NAME = "class.name";

	/**
	 * Dependency keys for the {@link HttpSessionObjectManagedObjectSource}.
	 */
	public static enum HttpSessionObjectDependencies {
		HTTP_SESSION
	}

	/**
	 * {@link Class} of the {@link Object}.
	 */
	private Class<?> clazz;

	/*
	 * ==================== AbstractManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME);
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HttpSessionObjectDependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class of the object
		String className = mosContext.getProperty(PROPERTY_CLASS_NAME);
		this.clazz = mosContext.getClassLoader().loadClass(className);

		// Load the meta-data
		context.setManagedObjectClass(HttpSessionObjectManagedObject.class);
		context.setObjectClass(this.clazz);
		context.addDependency(HttpSessionObjectDependencies.HTTP_SESSION,
				HttpSession.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionObjectManagedObject(this.clazz);
	}

}