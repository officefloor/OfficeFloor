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

import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;

/**
 * {@link ManagedObjectSource} for the {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectResponseManagedObjectSource
		extends AbstractManagedObjectSource<ObjectResponseManagedObjectSource.ObjectResponseDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum ObjectResponseDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link List} of {@link HttpObjectResponderFactory} instances.
	 */
	private final List<HttpObjectResponderFactory> objectResponderFactoriesList;

	/**
	 * {@link HttpObjectResponderFactory} instances.
	 */
	private HttpObjectResponderFactory[] factories;

	/**
	 * Instantiate.
	 * 
	 * @param objectResponderFactories
	 *            {@link List} of {@link HttpObjectResponderFactory} instances.
	 */
	public ObjectResponseManagedObjectSource(List<HttpObjectResponderFactory> objectResponderFactories) {
		this.objectResponderFactoriesList = objectResponderFactories;
	}

	/*
	 * ==================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<ObjectResponseDependencies, None> context) throws Exception {

		// Load the meta-data
		context.setObjectClass(ObjectResponse.class);
		context.setManagedObjectClass(ObjectResponseManagedObject.class);
		context.addDependency(ObjectResponseDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);

		// Create the listing of factories
		this.factories = this.objectResponderFactoriesList
				.toArray(new HttpObjectResponderFactory[this.objectResponderFactoriesList.size()]);
		if (this.factories.length == 0) {
			throw new Exception(
					"Must have at least one " + HttpObjectResponderFactory.class.getSimpleName() + " configured");
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ObjectResponseManagedObject<Object>();
	}

	/**
	 * {@link ObjectResponse} {@link ManagedObject}.
	 */
	private class ObjectResponseManagedObject<T>
			implements CoordinatingManagedObject<ObjectResponseDependencies>, ObjectResponse<T> {

		/*
		 * ==================== ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<ObjectResponseDependencies> registry) throws Throwable {
			// TODO Auto-generated method stub
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== ObjectResponse =======================
		 */

		@Override
		public void send(T object) throws HttpException {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * <code>content-type</code> cache object.
	 */
	private static class ContentTypeCache {

		/**
		 * <code>content-type</code>.
		 */
		private final String contentType;

		/**
		 * {@link HttpObjectResponderFactory}.
		 */
		private final HttpObjectResponderFactory factory;

		/**
		 * {@link ObjectResponderCache} items.
		 */
		private ObjectResponderCache[] responders;

		/**
		 * Instantiate.
		 * 
		 * @param contentType
		 *            <code>content-type</code>.
		 * @param factory
		 *            {@link HttpObjectResponderFactory} for the
		 *            <code>content-type</code>.
		 */
		private ContentTypeCache(String contentType, HttpObjectResponderFactory factory) {
			this.contentType = contentType;
			this.factory = factory;
		}
	}

	/**
	 * {@link ObjectResponse} cache object.
	 */
	private static class ObjectResponderCache<T> {

		/**
		 * Object type.
		 */
		private final Class<T> objectType;

		/**
		 * ObjectResponder
		 */
		private final HttpObjectResponder<T> objectResponder;

		/**
		 * Instantiate.
		 * 
		 * @param objectType
		 *            Object type.
		 * @param objectResponder
		 *            {@link HttpObjectResponder} for the object type.
		 */
		private ObjectResponderCache(Class<T> objectType, HttpObjectResponder<T> objectResponder) {
			this.objectType = objectType;
			this.objectResponder = objectResponder;
		}
	}

}