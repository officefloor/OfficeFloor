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
package net.officefloor.plugin.servlet;

import javax.servlet.Servlet;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.ServletBridge;

/**
 * {@link ManagedObjectSource} for a {@link Servlet} dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletDependencyManagedObjectSource
		extends
		AbstractManagedObjectSource<ServletDependencyManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys for the {@link ServletDependencyManagedObjectSource}.
	 */
	public static enum DependencyKeys {
		SERVLET_BRIDGE
	}

	/**
	 * Name of property for the type.
	 */
	public static final String PROPERTY_TYPE_NAME = "type.name";

	/**
	 * Type of dependency.
	 */
	private Class<?> type;

	/*
	 * =========================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TYPE_NAME, "Type");
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the dependency type
		String typeName = mosContext.getProperty(PROPERTY_TYPE_NAME);
		this.type = mosContext.loadClass(typeName);

		// Specify meta-data
		context.setObjectClass(this.type);
		context.setManagedObjectClass(ServletDependencyManagedObject.class);
		context.addDependency(DependencyKeys.SERVLET_BRIDGE,
				ServletBridge.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletDependencyManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link Servlet} dependency.
	 */
	public class ServletDependencyManagedObject implements
			CoordinatingManagedObject<DependencyKeys> {

		/**
		 * Dependency.
		 */
		private Object dependency;

		/*
		 * ==================== ManagedObject ===========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry)
				throws Throwable {

			// Obtain the Servlet bridge
			ServletBridge bridge = (ServletBridge) registry
					.getObject(DependencyKeys.SERVLET_BRIDGE);

			// Obtain the dependency
			this.dependency = bridge
					.getObject(ServletDependencyManagedObjectSource.this.type);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.dependency;
		}
	}

}