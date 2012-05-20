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

package net.officefloor.plugin.servlet.web.http.application;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.web.http.application.HttpRequestState;

/**
 * {@link HttpRequestState} implemented with the {@link ServletBridge} to use
 * the {@link ServletContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpRequestStateManagedObjectSource
		extends
		AbstractManagedObjectSource<ServletHttpRequestStateManagedObjectSource.Dependencies, None> {

	/**
	 * Dependencies for the {@link ServletHttpRequestStateManagedObject}.
	 */
	public static enum Dependencies {
		SERVLET_BRIDGE
	}

	/*
	 * ==================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		context.setObjectClass(HttpRequestState.class);
		context.setManagedObjectClass(ServletHttpRequestStateManagedObject.class);
		context.addDependency(Dependencies.SERVLET_BRIDGE, ServletBridge.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletHttpRequestStateManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link ServletBridge}.
	 */
	public static class ServletHttpRequestStateManagedObject implements
			CoordinatingManagedObject<Dependencies>, HttpRequestState {

		/**
		 * {@link HttpServletRequest}.
		 */
		private HttpServletRequest request;

		/*
		 * ==================== CoordinatingManagedObject ===================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {
			// Obtain the HTTP Servlet Request
			ServletBridge bridge = (ServletBridge) registry
					.getObject(Dependencies.SERVLET_BRIDGE);
			this.request = bridge.getRequest();
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ======================== HttpRequestState =======================
		 */

		@Override
		public Object getAttribute(String name) {
			synchronized (this.request) {
				return this.request.getAttribute(name);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public Iterator<String> getAttributeNames() {
			synchronized (this.request) {
				// Create copy of names to avoid concurrency issues
				List<String> list = new ArrayList<String>();
				for (Enumeration<String> names = this.request
						.getAttributeNames(); names.hasMoreElements();) {
					list.add(names.nextElement());
				}

				// Return iterator over names
				return list.iterator();
			}
		}

		@Override
		public void setAttribute(String name, Object object) {
			synchronized (this.request) {
				this.request.setAttribute(name, object);
			}
		}

		@Override
		public void removeAttribute(String name) {
			synchronized (this.request) {
				this.request.removeAttribute(name);
			}
		}
	}

}