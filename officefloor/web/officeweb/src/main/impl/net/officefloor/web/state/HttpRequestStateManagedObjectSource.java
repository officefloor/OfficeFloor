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
package net.officefloor.web.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.build.HttpObjectResponder;

/**
 * {@link ManagedObjectSource} for the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestStateManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/*
	 * =================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpRequestState.class);
		context.setManagedObjectClass(HttpRequestStateManagedObject.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpRequestStateManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link HttpRequestState}.
	 */
	private static class HttpRequestStateManagedObject implements ProcessAwareManagedObject, HttpRequestState {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private ProcessAwareContext context;

		/**
		 * {@link HttpObjectResponder} instances.
		 */
		private HttpObjectResponder<?>[] objectResponses;

		/**
		 * Attributes.
		 */
		private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

		/*
		 * ====================== ManagedObject ===========================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			this.context = context;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== HttpRequestState ==========================
		 */

		@Override
		public void setObjectResponses(HttpObjectResponder<?>[] objectResponses) {
			this.context.run(() -> {
				this.objectResponses = objectResponses;
				return null;
			});
		}

		@Override
		public HttpObjectResponder<?>[] getObjectResponses() {
			return this.context.run(() -> this.objectResponses);
		}

		@Override
		public Serializable getAttribute(String name) {
			return this.context.run(() -> this.attributes.get(name));
		}

		@Override
		public Iterator<String> getAttributeNames() {
			return this.context.run(() -> {
				// Create copy of names (stops concurrency issues)
				List<String> names = new ArrayList<String>(this.attributes.keySet());
				return names.iterator();
			});
		}

		@Override
		public void setAttribute(String name, Serializable object) {
			this.context.run(() -> this.attributes.put(name, object));
		}

		@Override
		public void removeAttribute(String name) {
			this.context.run(() -> this.attributes.remove(name));
		}

		@Override
		public Serializable exportState() throws IOException {
			return this.context.run(() -> {

				// Create the momento state
				Map<String, Serializable> momentoAttributes = new HashMap<String, Serializable>(this.attributes);

				// Create and return the momento
				return new StateMomento(momentoAttributes);
			});
		}

		@Override
		public void importState(Serializable momento) throws IOException, IllegalArgumentException {
			this.context.run(() -> {
				// Ensure valid state momento
				if (!(momento instanceof StateMomento)) {
					throw new IllegalArgumentException("Invalid momento for " + HttpRequestState.class.getSimpleName());
				}
				StateMomento state = (StateMomento) momento;

				// Load the state
				this.attributes = new HashMap<String, Serializable>(state.attributes);

				// Void return
				return null;
			});
		}
	}

	/**
	 * State momento.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Attributes.
		 */
		private final Map<String, Serializable> attributes;

		/**
		 * Initiate.
		 * 
		 * @param attributes
		 *            Attributes.
		 */
		public StateMomento(Map<String, Serializable> attributes) {
			this.attributes = attributes;
		}
	}

}