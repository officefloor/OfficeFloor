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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.value.load.ValueLoader;

/**
 * {@link ManagedObjectSource} for a HTTP argument.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpArgumentManagedObjectSource
		extends AbstractManagedObjectSource<HttpArgumentManagedObjectSource.HttpArgumentDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpArgumentDependencies {
		HTTP_REQUEST_STATE
	}

	/**
	 * Name of the parameter.
	 */
	private final String parameterName;

	/**
	 * {@link HttpValueLocation}.
	 */
	private final HttpValueLocation valueLocation;

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name of the parameter.
	 * @param valueLocation {@link HttpValueLocation}.
	 */
	public HttpArgumentManagedObjectSource(String parameterName, HttpValueLocation valueLocation) {
		this.parameterName = parameterName;
		this.valueLocation = valueLocation;
	}

	/*
	 * ================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpArgumentDependencies, None> context) throws Exception {
		context.setObjectClass(String.class);
		context.setManagedObjectClass(HttpArgumentManagedObject.class);
		context.addDependency(HttpArgumentDependencies.HTTP_REQUEST_STATE, HttpRequestState.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpArgumentManagedObject();
	}

	/**
	 * HTTP argument {@link ManagedObject}.
	 */
	private class HttpArgumentManagedObject
			implements CoordinatingManagedObject<HttpArgumentDependencies>, ValueLoader {

		/**
		 * Value.
		 */
		private String value = null;

		/*
		 * ================== ValueLoader ====================
		 */

		@Override
		public void loadValue(String name, String value, HttpValueLocation location) throws HttpException {

			// Ensure match on location
			if ((HttpArgumentManagedObjectSource.this.valueLocation != null)
					&& (HttpArgumentManagedObjectSource.this.valueLocation != location)) {
				return; // not match location
			}

			// Load if match on name
			if (HttpArgumentManagedObjectSource.this.parameterName.equals(name)) {
				this.value = value;
			}
		}

		/*
		 * ================== ManagedObject ====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<HttpArgumentDependencies> registry) throws Throwable {

			// Obtain the request state
			HttpRequestState requestState = (HttpRequestState) registry
					.getObject(HttpArgumentDependencies.HTTP_REQUEST_STATE);

			// Load the value
			requestState.loadValues(this);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.value;
		}
	}

}
