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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.http.HTTPException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.tokenise.HttpRequestTokeniser;
import net.officefloor.web.value.load.ValueLoader;

/**
 * {@link ManagedObjectSource} for the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpRequestStateManagedObjectSource
		extends AbstractManagedObjectSource<HttpRequestStateManagedObjectSource.HttpRequestStateDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpRequestStateDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Initialises the {@link HttpRequestState}.
	 * 
	 * @param pathArguments
	 *            Head path {@link HttpArgument} of the linked list of
	 *            {@link HttpArgument} instances.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 */
	public static void initialiseHttpRequestState(HttpArgument pathArguments, HttpRequestState requestState) {
		HttpRequestStateManagedObject mo = (HttpRequestStateManagedObject) requestState;
		mo.initialise(pathArguments);
	}

	/**
	 * Imports the state from the momento.
	 * 
	 * @param momento
	 *            Momento containing the state for the {@link HttpRequestState}.
	 * @param requestState
	 *            {@link HttpRequestState}
	 * @throws IllegalArgumentException
	 *             If invalid momento.
	 */
	public static void importHttpRequestState(Serializable momento, HttpRequestState requestState)
			throws IllegalArgumentException {
		// Ensure valid state momento
		if (!(momento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for " + HttpRequestState.class.getSimpleName());
		}
		StateMomento state = (StateMomento) momento;
		HttpRequestStateManagedObject mo = (HttpRequestStateManagedObject) requestState;
		mo.context.run(() -> {

			// Load the arguments
			mo.arguments = state.headArgument;
			mo.isTokenisedRequest = true;

			// Load the state
			mo.attributes = new HashMap<String, Serializable>(state.attributes);

			// Void return
			return null;
		});
	}

	/**
	 * Exports a momento for the current state of this {@link HttpRequestState}.
	 *
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @return Momento for the current state of this {@link HttpRequestState}.
	 */
	public static Serializable exportHttpRequestState(HttpRequestState requestState) {
		HttpRequestStateManagedObject mo = (HttpRequestStateManagedObject) requestState;
		return mo.context.run(() -> {

			// Obtain the arguments
			mo.ensureTokenised();
			HttpArgument headArgument = mo.arguments;

			// Create the momento state
			Map<String, Serializable> momentoAttributes = new HashMap<String, Serializable>(mo.attributes);

			// Create and return the momento
			return new StateMomento(headArgument, momentoAttributes);
		});
	}

	/**
	 * {@link HttpArgumentParser} instances.
	 */
	private final HttpArgumentParser[] argumentParsers;

	/**
	 * Instantiate.
	 * 
	 * @param argumentParsers
	 *            {@link HttpArgumentParser} instances.
	 */
	public HttpRequestStateManagedObjectSource(HttpArgumentParser[] argumentParsers) {
		this.argumentParsers = argumentParsers;
	}

	/*
	 * =================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<HttpRequestStateDependencies, None> context) throws Exception {
		context.setObjectClass(HttpRequestState.class);
		context.setManagedObjectClass(HttpRequestStateManagedObject.class);
		context.addDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpRequestStateManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link HttpRequestState}.
	 */
	private class HttpRequestStateManagedObject implements ProcessAwareManagedObject,
			CoordinatingManagedObject<HttpRequestStateDependencies>, HttpRequestState, ValueLoader {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private ProcessAwareContext context;

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/**
		 * Head {@link HttpArgument} of the linked list of {@link HttpArgument}
		 * instances.
		 */
		private HttpArgument arguments = null;

		/**
		 * Indicates if tokenised the {@link HttpRequest}.
		 */
		private boolean isTokenisedRequest = false;

		/**
		 * Attributes.
		 */
		private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

		/**
		 * Initialises this {@link HttpRequestState}.
		 * 
		 * @param pathArguments
		 *            Head path {@link HttpArgument} of the linked list of
		 *            {@link HttpArgument} instances.
		 */
		private void initialise(HttpArgument pathArguments) {
			this.context.run(() -> {
				this.arguments = pathArguments;
				return null;
			});
		}

		/**
		 * Ensures the {@link HttpRequest} has been tokenized.
		 * 
		 * @return {@link HttpRequest}.
		 */
		private HttpRequest ensureTokenised() {
			HttpRequest request = this.connection.getRequest();
			if (!this.isTokenisedRequest) {

				// Tokenise out the arguments
				HttpRequestTokeniser.tokeniseHttpRequest(request,
						HttpRequestStateManagedObjectSource.this.argumentParsers, this);

				// Request now tokenised
				this.isTokenisedRequest = true;
			}
			return request;
		}

		/*
		 * ====================== ManagedObject ===========================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			this.context = context;
		}

		@Override
		public void loadObjects(ObjectRegistry<HttpRequestStateDependencies> registry) throws Throwable {
			this.connection = (ServerHttpConnection) registry
					.getObject(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ValueLoader =========================
		 */

		@Override
		public void loadValue(String name, String value, HttpValueLocation location) throws HTTPException {
			HttpArgument oldHead = this.arguments;
			this.arguments = new HttpArgument(name, value, location);
			this.arguments.next = oldHead;
		}

		/*
		 * ==================== HttpRequestState ==========================
		 */

		@Override
		public void loadValues(ValueLoader valueLoader) throws HttpException {
			this.context.run(() -> {

				// Tokenise the HTTP request
				HttpRequest request = this.ensureTokenised();

				// Load the arguments
				HttpArgument argument = this.arguments;
				while (argument != null) {
					valueLoader.loadValue(argument.name, argument.value, argument.location);
					argument = argument.next;
				}

				// Always load cookies from request
				for (HttpRequestCookie cookie : request.getCookies()) {
					valueLoader.loadValue(cookie.getName(), cookie.getValue(), HttpValueLocation.COOKIE);
				}

				// Void return
				return null;
			});
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
	}

	/**
	 * State momento.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Head {@link HttpArgument}.
		 */
		private final HttpArgument headArgument;

		/**
		 * Attributes.
		 */
		private final Map<String, Serializable> attributes;

		/**
		 * Initiate.
		 * 
		 * @param headArgument
		 *            Head {@link HttpArgument}.
		 * @param attributes
		 *            Attributes.
		 */
		public StateMomento(HttpArgument headArgument, Map<String, Serializable> attributes) {
			this.headArgument = headArgument;
			this.attributes = attributes;
		}
	}

}