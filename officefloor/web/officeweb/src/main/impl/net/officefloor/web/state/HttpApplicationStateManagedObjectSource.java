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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedObjectSource} for the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationStateManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, HttpApplicationState {

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * Attributes.
	 */
	private final Map<String, Object> attributes = new ConcurrentHashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	public HttpApplicationStateManagedObjectSource(String contextPath) {
		this.contextPath = contextPath;
	}

	/*
	 * =================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpApplicationState.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

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
	public String getContextPath() {
		return this.contextPath;
	}

	@Override
	public String createApplicationClientUrl(boolean isSecure, String path, ServerHttpConnection connection) {

		// Create the application path
		path = this.createApplicationClientPath(path);

		// Determine if appropriately secure
		if (connection.isSecure() == isSecure) {
			// Relative path
			return path;
		} else {
			// Full path back to server
			return connection.getServerLocation().createClientUrl(isSecure, path);
		}
	}

	@Override
	public String createApplicationClientPath(String path) {

		// Create the application path
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}

		// Return the path
		return path;
	}

	@Override
	public Object getAttribute(String name) {
		return HttpApplicationStateManagedObjectSource.this.attributes.get(name);
	}

	@Override
	public Iterator<String> getAttributeNames() {
		// Create copy of names (stops concurrency issues)
		List<String> names = new ArrayList<String>(this.attributes.keySet());
		return names.iterator();
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.attributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

}