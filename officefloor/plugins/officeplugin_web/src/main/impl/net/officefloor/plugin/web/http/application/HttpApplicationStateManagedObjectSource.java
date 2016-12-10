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
public class HttpApplicationStateManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Attributes.
	 */
	private final Map<String, Object> attributes = new HashMap<String, Object>();

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
		context.setObjectClass(HttpApplicationState.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpRequestStateManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link HttpRequestState}.
	 */
	private class HttpRequestStateManagedObject implements ManagedObject,
			HttpApplicationState {

		/*
		 * ====================== ManagedObject ===========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== HttpApplicationState ==========================
		 */

		@Override
		public Object getAttribute(String name) {
			synchronized (HttpApplicationStateManagedObjectSource.this.attributes) {
				return HttpApplicationStateManagedObjectSource.this.attributes
						.get(name);
			}
		}

		@Override
		public synchronized Iterator<String> getAttributeNames() {
			synchronized (HttpApplicationStateManagedObjectSource.this.attributes) {
				// Create copy of names (stops concurrency issues)
				List<String> names = new ArrayList<String>(
						HttpApplicationStateManagedObjectSource.this.attributes
								.keySet());
				return names.iterator();
			}
		}

		@Override
		public synchronized void setAttribute(String name, Object object) {
			synchronized (HttpApplicationStateManagedObjectSource.this.attributes) {
				HttpApplicationStateManagedObjectSource.this.attributes.put(
						name, object);
			}
		}

		@Override
		public synchronized void removeAttribute(String name) {
			synchronized (HttpApplicationStateManagedObjectSource.this.attributes) {
				HttpApplicationStateManagedObjectSource.this.attributes
						.remove(name);
			}
		}
	}

}