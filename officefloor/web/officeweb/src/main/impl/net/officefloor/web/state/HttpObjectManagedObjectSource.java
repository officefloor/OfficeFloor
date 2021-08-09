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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.WebArchitect;

/**
 * {@link ManagedObjectSource} to provide an {@link Object} from the
 * {@link HttpObjectParser} instances.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpObjectManagedObjectSource<T>
		extends AbstractManagedObjectSource<HttpObjectManagedObjectSource.HttpObjectDependencies, None> {

	/**
	 * Obtains the default {@link HttpObjectParserFactory}.
	 */
	@FunctionalInterface
	public static interface DefaultHttpObjectParser {

		/**
		 * Obtains the default {@link HttpObjectParserFactory}.
		 * 
		 * @return Default {@link HttpObjectParserFactory}.
		 * @throws Exception If fails to obtain the default
		 *                   {@link HttpObjectParserFactory}.
		 */
		HttpObjectParserFactory getDefaultHttpObjectParserFactory() throws Exception;
	}

	/**
	 * Dependency keys.
	 */
	public static enum HttpObjectDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Object} {@link Class}.
	 */
	private final Class<T> objectClass;

	/**
	 * Accepted <code>content-type</code> values.
	 */
	private final String[] acceptedContentTypes;

	/**
	 * {@link List} of {@link HttpObjectParserFactory} instances that are loaded by
	 * the {@link WebArchitect}.
	 */
	private final List<HttpObjectParserFactory> parserFactories;

	/**
	 * {@link DefaultHttpObjectParser}.
	 */
	private final DefaultHttpObjectParser defaultHttpObjectParser;

	/**
	 * {@link HttpObjectParser} instances.
	 */
	private HttpObjectParser<T>[] parsers;

	/**
	 * Instantiate.
	 * 
	 * @param objectClass             {@link Object} {@link Class}.
	 * @param acceptedContentTypes    Accepted <code>content-type</code> values.
	 * @param parserFactories         {@link List} of
	 *                                {@link HttpObjectParserFactory} instances that
	 *                                are loaded by the {@link WebArchitect}.
	 * @param defaultHttpObjectParser {@link DefaultHttpObjectParser}.
	 */
	public HttpObjectManagedObjectSource(Class<T> objectClass, String[] acceptedContentTypes,
			List<HttpObjectParserFactory> parserFactories, DefaultHttpObjectParser defaultHttpObjectParser) {
		this.objectClass = objectClass;
		this.acceptedContentTypes = acceptedContentTypes;
		this.parserFactories = parserFactories;
		this.defaultHttpObjectParser = defaultHttpObjectParser;
	}

	/*
	 * ==================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<HttpObjectDependencies, None> context) throws Exception {

		// Load the meta-data
		context.setObjectClass(this.objectClass);
		context.setManagedObjectClass(HttpObjectManagedObject.class);
		context.addDependency(HttpObjectDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);

		// Create filter message
		String acceptContentTypeMessage = " in any Content-Type";
		if ((this.acceptedContentTypes != null) && (this.acceptedContentTypes.length > 0)) {
			acceptContentTypeMessage = " for accepting Content-Type"
					+ ((this.acceptedContentTypes.length > 2) ? "s " : " ")
					+ String.join(", ", this.acceptedContentTypes);
		}

		// Create the list of parsers
		List<HttpObjectParser<T>> objectParsers = new LinkedList<>();
		for (HttpObjectParserFactory parserFactory : this.parserFactories) {
			this.loadObjectParser(parserFactory, objectParsers);
		}

		// Determine if default parser
		if (objectParsers.size() == 0) {
			HttpObjectParserFactory parserFactory = this.defaultHttpObjectParser.getDefaultHttpObjectParserFactory();
			if (parserFactory != null) {
				this.loadObjectParser(parserFactory, objectParsers);
			}
		}

		// Load the parsers
		this.parsers = objectParsers.toArray(new HttpObjectParser[objectParsers.size()]);

		// Ensure have at least one object parser
		if (this.parsers.length == 0) {
			throw new Exception("No " + HttpObjectParser.class.getSimpleName() + " available for object "
					+ this.objectClass.getName() + acceptContentTypeMessage);
		}
	}

	/**
	 * Loads the {@link HttpObjectParserFactory}.
	 * 
	 * @param parserFactory {@link HttpObjectParserFactory}.
	 * @param objectParsers {@link List} to be loaded with the
	 *                      {@link HttpObjectParser}.
	 */
	private void loadObjectParser(HttpObjectParserFactory parserFactory, List<HttpObjectParser<T>> objectParsers)
			throws Exception {

		// Obtain the content type
		String contentType = parserFactory.getContentType();

		// Determine if accept Content-Type
		if ((this.acceptedContentTypes != null) && (this.acceptedContentTypes.length > 0)) {
			// Ensure one of the accepted content types
			boolean isAcceptableContentType = false;
			for (String acceptedContentType : this.acceptedContentTypes) {
				if (acceptedContentType.equals(contentType)) {
					isAcceptableContentType = true;
				}
			}
			if (!isAcceptableContentType) {
				// Not acceptable Content-Type
				return;
			}
		}

		// Create the parser for the object class
		try {
			HttpObjectParser<T> parser = parserFactory.createHttpObjectParser(this.objectClass);
			if (parser == null) {
				return; // parser not able to handle object
			}

			// Include the parser
			objectParsers.add(parser);

		} catch (Exception ex) {
			// Propagate failure about creating object parser
			String errorMessage = ex.getMessage();
			errorMessage = CompileUtil.isBlank(errorMessage) ? "" : " (cause: " + errorMessage + ")";
			throw new Exception("Failed to create " + HttpObjectParser.class.getSimpleName() + " for Content-Type "
					+ contentType + " for object " + this.objectClass.getName() + errorMessage, ex);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpObjectManagedObject();
	}

	/**
	 * HTTP Object {@link ManagedObject}.
	 */
	private class HttpObjectManagedObject implements CoordinatingManagedObject<HttpObjectDependencies> {

		/**
		 * HTTP object.
		 */
		private Object httpObject;

		/*
		 * ================= ManagedObject =========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<HttpObjectDependencies> registry) throws Throwable {

			// Obtain the server HTTP connection
			ServerHttpConnection connection = (ServerHttpConnection) registry
					.getObject(HttpObjectDependencies.SERVER_HTTP_CONNECTION);

			// Obtain the content type
			HttpRequest request = connection.getRequest();
			HttpHeader header = request.getHeaders().getHeader("content-type");
			if (header == null) {
				// No content-type so can not parse out object
				this.httpObject = null;
				return;
			}
			String contentType = header.getValue();

			// Search the parsers for matching content type
			for (int i = 0; i < HttpObjectManagedObjectSource.this.parsers.length; i++) {
				HttpObjectParser<T> parser = HttpObjectManagedObjectSource.this.parsers[i];

				// Determine if can handle content-type
				if (contentType.equals(parser.getContentType())) {
					// Found parser for the object
					this.httpObject = parser.parse(connection);
					return; // parsed out the object
				}
			}

			// As here, no HTTP object
			this.httpObject = null;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.httpObject;
		}
	}

}
