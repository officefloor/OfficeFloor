/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpEscalationContext;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.accept.AcceptNegotiatorBuilderImpl;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * {@link ManagedObjectSource} for the {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class ObjectResponseManagedObjectSource
		extends AbstractManagedObjectSource<ObjectResponseManagedObjectSource.ObjectResponseDependencies, None>
		implements HttpEscalationHandler {

	/**
	 * Obtains the default {@link HttpObjectResponderFactory}.
	 */
	@FunctionalInterface
	public static interface DefaultHttpObjectResponder {

		/**
		 * Obtains the default {@link HttpObjectResponderFactory}.
		 * 
		 * @return Default {@link HttpObjectResponderFactory}.
		 * @throws Exception If fails to obtain the default
		 *                   {@link HttpObjectResponderFactory}.
		 */
		HttpObjectResponderFactory getDefaultHttpObjectResponderFactory() throws Exception;
	}

	/**
	 * Dependency keys.
	 */
	public static enum ObjectResponseDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Response {@link HttpStatus}.
	 */
	private final HttpStatus httpStatus;

	/**
	 * {@link List} of {@link HttpObjectResponderFactory} instances.
	 */
	private final List<HttpObjectResponderFactory> objectResponderFactoriesList;

	/**
	 * {@link DefaultHttpObjectResponder}.
	 */
	private final DefaultHttpObjectResponder defaultHttpObjectResponder;

	/**
	 * {@link AcceptNegotiator} for the {@link Object} {@link ContentTypeCache}.
	 */
	private AcceptNegotiator<ContentTypeCache> objectNegotiator;

	/**
	 * {@link AcceptNegotiator} for the {@link Escalation} {@link ContentTypeCache}.
	 */
	private AcceptNegotiator<ContentTypeCache> escalationNegotiator;

	/**
	 * {@link WritableHttpHeader} instances when not acceptable type requested.
	 */
	private WritableHttpHeader[] notAcceptableHeaders;

	/**
	 * Instantiate.
	 * 
	 * @param httpStatus                 {@link HttpStatus}.
	 * @param objectResponderFactories   {@link List} of
	 *                                   {@link HttpObjectResponderFactory}
	 *                                   instances.
	 * @param defaultHttpObjectResponder {@link DefaultHttpObjectResponder}.
	 */
	public ObjectResponseManagedObjectSource(HttpStatus httpStatus,
			List<HttpObjectResponderFactory> objectResponderFactories,
			DefaultHttpObjectResponder defaultHttpObjectResponder) {
		this.httpStatus = httpStatus;
		this.objectResponderFactoriesList = objectResponderFactories;
		this.defaultHttpObjectResponder = defaultHttpObjectResponder;
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

		// Create the not acceptable headers
		StringBuilder accept = new StringBuilder();
		boolean isFirst = true;

		// Create the negotiators
		AcceptNegotiatorBuilder<ContentTypeCache> objectBuilder = new AcceptNegotiatorBuilderImpl<>();
		AcceptNegotiatorBuilder<ContentTypeCache> escalationBuilder = new AcceptNegotiatorBuilderImpl<>();
		NEXT_FACTORY: for (HttpObjectResponderFactory factory : this.objectResponderFactoriesList) {
			String contentType = factory.getContentType();
			if (CompileUtil.isBlank(contentType)) {
				continue NEXT_FACTORY;
			}

			// Add content-type for negotiator
			objectBuilder.addHandler(contentType, new ContentTypeCache(factory));
			escalationBuilder.addHandler(contentType, new ContentTypeCache(factory));

			// Include in accept header response
			if (!isFirst) {
				accept.append(", ");
			}
			isFirst = false;
			accept.append(contentType);
		}
		if (isFirst) {
			// Determine if provide default responder
			HttpObjectResponderFactory defaultFactory = this.defaultHttpObjectResponder
					.getDefaultHttpObjectResponderFactory();
			if (defaultFactory != null) {

				// Provide default
				String contentType = defaultFactory.getContentType();
				if (!CompileUtil.isBlank(contentType)) {

					// Add content-type for negotiator
					objectBuilder.addHandler(contentType, new ContentTypeCache(defaultFactory));
					escalationBuilder.addHandler(contentType, new ContentTypeCache(defaultFactory));

					// Only the one, so is the accept type
					accept.append(contentType);
				}
			}
		}
		try {
			this.objectNegotiator = objectBuilder.build();
			this.escalationNegotiator = escalationBuilder.build();
		} catch (NoAcceptHandlersException ex) {
			throw new Exception(
					"Must have at least one " + HttpObjectResponderFactory.class.getSimpleName() + " configured");
		}

		// Create the not acceptable headers
		this.notAcceptableHeaders = new WritableHttpHeader[] {
				new WritableHttpHeader(new HttpHeaderName("accept"), new HttpHeaderValue(accept.toString())) };
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ObjectResponseManagedObject<Object>();
	}

	/**
	 * {@link ObjectResponse} {@link ManagedObject}.
	 */
	private class ObjectResponseManagedObject<T> implements ContextAwareManagedObject,
			CoordinatingManagedObject<ObjectResponseDependencies>, ObjectResponse<T> {

		/**
		 * {@link ManagedObjectContext}.
		 */
		private ManagedObjectContext context;

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/**
		 * Accepted {@link ContentTypeCache}.
		 */
		private ContentTypeCache contentTypeCache = null;

		/*
		 * ==================== ManagedObject =======================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.context = context;
		}

		@Override
		public void loadObjects(ObjectRegistry<ObjectResponseDependencies> registry) throws Throwable {

			// Obtain the server HTTP connection
			this.connection = (ServerHttpConnection) registry
					.getObject(ObjectResponseDependencies.SERVER_HTTP_CONNECTION);
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
			this.context.run(() -> {

				// Lazy obtain the content type cache
				if (this.contentTypeCache == null) {
					this.contentTypeCache = ObjectResponseManagedObjectSource.this.objectNegotiator
							.getHandler(this.connection.getRequest());
				}

				// Ensure have acceptable content type
				if (this.contentTypeCache == null) {
					throw new HttpException(HttpStatus.NOT_ACCEPTABLE,
							ObjectResponseManagedObjectSource.this.notAcceptableHeaders, null);
				}

				// Provide response status
				this.connection.getResponse().setStatus(ObjectResponseManagedObjectSource.this.httpStatus);

				// Handle the object
				handleObject(object, this.contentTypeCache, OBJECT_RESPONDER_FACTORY, this.connection);
				return null;
			});
		}
	}

	/*
	 * ==================== HttpEscalationHandler ====================
	 */

	@Override
	public boolean handle(HttpEscalationContext context) throws IOException {

		// Obtain the connection
		ServerHttpConnection connection = context.getServerHttpConnection();

		// Obtain the acceptable content type
		ContentTypeCache contentTypeCache = this.escalationNegotiator.getHandler(connection.getRequest());
		if (contentTypeCache == null) {
			return false; // not able to handle escalation
		}

		// Obtain the escalation
		Throwable escalation = context.getEscalation();

		// Handle escalation
		handleObject(escalation, contentTypeCache, ESCALATION_RESPONDER_FACTORY, connection);
		return true; // handled
	}

	/**
	 * Object {@link ResponderFactory}.
	 */
	private static ResponderFactory OBJECT_RESPONDER_FACTORY = new ResponderFactory() {
		@Override
		public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType,
				HttpObjectResponderFactory factory) {
			return factory.createHttpObjectResponder(objectType);
		}
	};

	/**
	 * {@link Escalation} {@link ResponderFactory}.
	 */
	private static ResponderFactory ESCALATION_RESPONDER_FACTORY = new ResponderFactory() {
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public <E> HttpObjectResponder<E> createHttpObjectResponder(Class<E> objectType,
				HttpObjectResponderFactory factory) {
			Class escalationType = objectType;
			return factory.createHttpEscalationResponder(escalationType);
		}
	};

	/**
	 * Responder factory.
	 */
	private static interface ResponderFactory {

		/**
		 * Creates the {@link HttpObjectResponder}.
		 * 
		 * @param objectType Object type.
		 * @param factory    {@link HttpObjectResponderFactory}.
		 * @return {@link HttpObjectResponder}.
		 */
		<T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType, HttpObjectResponderFactory factory);
	}

	/**
	 * Handles the object.
	 * 
	 * @param object           Object for the response.
	 * @param head             Head {@link AcceptType} for the linked list of
	 *                         {@link AcceptType} instances.
	 * @param cache            {@link ContentTypeCache} instances.
	 * @param responderFactory {@link ResponderFactory}.
	 * @param connection       {@link ServerHttpConnection} connection.
	 * @return <code>true</code> if object sent.
	 */
	@SuppressWarnings("unchecked")
	private static <T> void handleObject(T object, ContentTypeCache contentTypeCache, ResponderFactory responderFactory,
			ServerHttpConnection connection) {

		// Obtain the object type
		Class<T> objectType = (Class<T>) object.getClass();

		// Find the corresponding type
		HttpObjectResponder<T> objectResponder = null;
		FIND_RESPONDER: for (int j = 0; j < contentTypeCache.responders.length; j++) {
			ObjectResponderCache<?> responder = contentTypeCache.responders[j];
			if (responder.objectType == objectType) {
				objectResponder = (HttpObjectResponder<T>) responder.objectResponder;
				break FIND_RESPONDER;
			}
		}
		if (objectResponder == null) {
			// Need to create object responder for type
			objectResponder = responderFactory.createHttpObjectResponder(objectType, contentTypeCache.factory);
			ObjectResponderCache<T> responder = new ObjectResponderCache<>(objectType, objectResponder);

			// Append the object responder to cache
			ObjectResponderCache<?>[] responders = Arrays.copyOf(contentTypeCache.responders,
					contentTypeCache.responders.length + 1);
			responders[responders.length - 1] = responder;
			contentTypeCache.responders = responders;
		}

		// Send the response
		try {
			objectResponder.send(object, connection);
		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

	/**
	 * <code>content-type</code> cache object.
	 */
	private static class ContentTypeCache {

		/**
		 * {@link HttpObjectResponderFactory}.
		 */
		private final HttpObjectResponderFactory factory;

		/**
		 * {@link ObjectResponderCache} items.
		 */
		private ObjectResponderCache<?>[] responders = new ObjectResponderCache[0];

		/**
		 * Instantiate.
		 * 
		 * @param factory {@link HttpObjectResponderFactory} for the
		 *                <code>content-type</code>.
		 */
		private ContentTypeCache(HttpObjectResponderFactory factory) {
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
		 * @param objectType      Object type.
		 * @param objectResponder {@link HttpObjectResponder} for the object type.
		 */
		private ObjectResponderCache(Class<T> objectType, HttpObjectResponder<T> objectResponder) {
			this.objectType = objectType;
			this.objectResponder = objectResponder;
		}
	}

}
