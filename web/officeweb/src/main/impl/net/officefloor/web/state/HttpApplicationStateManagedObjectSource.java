/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.route.WebRouter;

/**
 * {@link ManagedObjectSource} for the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
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
	public String extractApplicationPath(ServerHttpConnection connection) throws HttpException {
		return WebRouter.transformToApplicationCanonicalPath(connection.getRequest().getUri(), this.contextPath);
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
