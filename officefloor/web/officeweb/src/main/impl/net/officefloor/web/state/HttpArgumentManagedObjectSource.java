/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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