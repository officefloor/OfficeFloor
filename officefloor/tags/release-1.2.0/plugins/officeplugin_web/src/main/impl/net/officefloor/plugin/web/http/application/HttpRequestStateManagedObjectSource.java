/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestStateManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/*
	 * =================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		context.setObjectClass(HttpRequestState.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpRequestStateManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link HttpRequestState}.
	 */
	private static class HttpRequestStateManagedObject implements
			ManagedObject, HttpRequestState {

		/**
		 * Attributes.
		 */
		private final Map<String, Object> attributes = new HashMap<String, Object>();

		/*
		 * ====================== ManagedObject ===========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== HttpRequestState ==========================
		 */

		@Override
		public synchronized Object getAttribute(String name) {
			return this.attributes.get(name);
		}

		@Override
		public synchronized Iterator<String> getAttributeNames() {
			// Create copy of names (stops concurrency issues)
			List<String> names = new ArrayList<String>(this.attributes.keySet());
			return names.iterator();
		}

		@Override
		public synchronized void setAttribute(String name, Object object) {
			this.attributes.put(name, object);
		}

		@Override
		public synchronized void removeAttribute(String name) {
			this.attributes.remove(name);
		}
	}

}