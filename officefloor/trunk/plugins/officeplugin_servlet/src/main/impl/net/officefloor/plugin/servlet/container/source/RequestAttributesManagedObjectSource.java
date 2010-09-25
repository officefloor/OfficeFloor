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
package net.officefloor.plugin.servlet.container.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * <p>
 * {@link ManagedObjectSource} for request attributes.
 * <p>
 * This should be used at {@link ManagedObjectScope#PROCESS} so to appropriately
 * reflect the request scope for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestAttributesManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/*
	 * ================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		// Specify meta-data
		context.setObjectClass(Map.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new RequestAttributesManagedObject();
	}

	/**
	 * Request attributes {@link ManagedObject}.
	 */
	private static class RequestAttributesManagedObject implements
			ManagedObject {

		/**
		 * Request attributes.
		 */
		private final Map<String, Object> attributes = Collections
				.synchronizedMap(new HashMap<String, Object>());

		/*
		 * ===================== ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.attributes;
		}
	}

}