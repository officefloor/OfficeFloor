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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
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
	 * {@link AcceptType} linked list to use should there be no
	 * <code>accept</code> {@link HttpHeader} values.
	 */
	private static final AcceptType MATCH_ANY = new AnyAcceptType("1", 0);

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
	 * {@link ContentTypeCache} instances for each
	 * {@link HttpObjectResponderFactory}.
	 */
	private ContentTypeCache[] cache;

	/**
	 * {@link WritableHttpHeader} instances when not acceptable type requested.
	 */
	private WritableHttpHeader[] notAcceptableHeaders;

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

		// Create the cache of factories
		this.cache = new ContentTypeCache[this.objectResponderFactoriesList.size()];
		for (int i = 0; i < this.cache.length; i++) {
			HttpObjectResponderFactory factory = this.objectResponderFactoriesList.get(i);
			String contentType = factory.getContentType();
			this.cache[i] = new ContentTypeCache(contentType, factory);
		}
		if (this.cache.length == 0) {
			throw new Exception(
					"Must have at least one " + HttpObjectResponderFactory.class.getSimpleName() + " configured");
		}

		// Create the not acceptable headers
		StringBuilder accept = new StringBuilder();
		boolean isFirst = true;
		for (ContentTypeCache item : this.cache) {
			if (!isFirst) {
				accept.append(", ");
			}
			isFirst = false;
			accept.append(item.contentType);
		}
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
	private class ObjectResponseManagedObject<T> implements ProcessAwareManagedObject,
			CoordinatingManagedObject<ObjectResponseDependencies>, ObjectResponse<T> {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private ProcessAwareContext context;

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/**
		 * Head {@link AcceptType} of the linked list of {@link AcceptType}
		 * instances.
		 */
		private AcceptType head = null;

		/*
		 * ==================== ManagedObject =======================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
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
		@SuppressWarnings("unchecked")
		public void send(T object) throws HttpException {
			this.context.run(() -> {

				// Parse out the accept types from request
				if (this.head == null) {

					// Load the accept types
					HttpRequestHeaders headers = this.connection.getHttpRequest().getHttpHeaders();
					for (HttpHeader header : headers.getHeaders("accept")) {
						this.head = parseAccept(header.getValue(), this.head);
					}

					// Determine if only wild card match
					// - no head, so will match any type
					// - only one head that is any match, so will match any type
					boolean isOnlyWildcard = ((this.head == null)
							|| ((this.head.next == null) && (this.head instanceof AnyAcceptType)));

					// Default to content-type if wild card only
					if (isOnlyWildcard) {

						// Attempt to match first on input content type
						// (e.g. if JSON sent then respond with JSON)
						HttpHeader contentTypeHeader = headers.getHeader("content-type");
						if (contentTypeHeader != null) {
							this.head = new SubTypeAcceptType(contentTypeHeader.getValue(), "1", 0);
						}

						// Now match any
						if (this.head == null) {
							this.head = MATCH_ANY;
						} else {
							this.head.next = MATCH_ANY;
						}
					}
				}

				// Obtain the object type
				Class<T> objectType = (Class<T>) object.getClass();

				// Attempt to find match
				AcceptType accept = this.head;
				while (accept != null) {

					// Attempt to obtain matching responder
					ContentTypeCache[] cache = ObjectResponseManagedObjectSource.this.cache;
					for (int i = 0; i < cache.length; i++) {
						ContentTypeCache item = cache[i];
						if (accept.isMatch(item.contentType)) {

							// Find the corresponding type
							HttpObjectResponder<T> objectResponder = null;
							FIND_RESPONDER: for (int j = 0; j < item.responders.length; j++) {
								ObjectResponderCache<?> responder = item.responders[j];
								if (responder.objectType == objectType) {
									objectResponder = (HttpObjectResponder<T>) responder.objectResponder;
									break FIND_RESPONDER;
								}
							}
							if (objectResponder == null) {
								// Need to create object responder for type
								objectResponder = item.factory.createHttpObjectResponder(objectType);
								ObjectResponderCache<T> responder = new ObjectResponderCache<>(objectType,
										objectResponder);

								// Append the object responder to cache
								ObjectResponderCache<?>[] responders = Arrays.copyOf(item.responders,
										item.responders.length + 1);
								responders[responders.length - 1] = responder;
								item.responders = responders;
							}

							// Send the response
							try {
								objectResponder.send(object, this.connection);
							} catch (IOException ex) {
								throw new HttpException(ex);
							}

							// Response sent
							return null;
						}
					}

					// Attempt next accept
					accept = accept.next;
				}

				// As here, not able to send acceptable content type
				throw new HttpException(HttpStatus.NOT_ACCEPTABLE,
						ObjectResponseManagedObjectSource.this.notAcceptableHeaders, null);
			});
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
		private ObjectResponderCache<?>[] responders = new ObjectResponderCache[0];

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

	/**
	 * State of parsing.
	 */
	private static enum ParseState {
		NEW_ACCEPT, TYPE, SUB_TYPE, PARAMETER_START, PARAMETER_NAME, PARAMETER_VALUE_START, PARAMETER_VALUE
	}

	/**
	 * Indicates if the character is a white space.
	 * 
	 * @param character
	 *            Character.
	 * @return <code>true</code> if character is white space.
	 */
	private static final boolean isWhiteSpace(char character) {
		return (character == ' ') || (character == '\t');
	}

	/**
	 * Parses the <code>accept</code> {@link HttpHeader} value returning the
	 * head {@link AcceptType} of the linked list of {@link AcceptType}
	 * instances.
	 * 
	 * @param accept
	 *            <code>accept</code> {@link HttpHeader} value.
	 * @param head
	 *            Head {@link AcceptType} from another
	 *            <code>accept<code> {@link HttpHeader} should there be multiple <code>accept</code>
	 *            {@link HttpHeader} values. Will be <code>null</code> if no
	 *            other <code>accept</code> {@link HttpHeader}.
	 * @return Head {@link AcceptType} for parsed out linked list of
	 *         {@link AcceptType} instances. The values are sorted with highest
	 *         weighted first.
	 * @throws HttpException
	 *             If invalid <code>accept</code> value.
	 */
	private static final AcceptType parseAccept(String accept, AcceptType head) throws HttpException {

		// State for parsing
		ParseState state = ParseState.NEW_ACCEPT;
		int typeStart = -1;
		int typeSeparatorPosition = -1;
		int subTypeEnd = -1;
		int paramStart = -1;
		int paramEnd = -1;
		boolean isParamEnd = false;
		boolean isQ = false;
		String q = "0";
		int parameterCount = 0;

		// Parse out the accept types
		NEXT_CHARACTER: for (int index = 0; index < accept.length(); index++) {
			char character = accept.charAt(index);

			// Handle based on state
			switch (state) {
			case NEW_ACCEPT:

				// Determine if load previous accept type
				if (typeStart != -1) {
					// Load the previous accept type
					head = loadAcceptType(accept, typeStart, typeSeparatorPosition, subTypeEnd, q, parameterCount,
							head);

					// Reset if multiple spaces
					typeStart = -1;
				}

				// Ignore leading space
				if (isWhiteSpace(character)) {
					continue NEXT_CHARACTER;
				}

				// Start of type
				typeStart = index;
				subTypeEnd = -1; // reset to find
				q = "0";
				parameterCount = 0; // reset for new accept type
				state = ParseState.TYPE;
				break;

			case TYPE:
				// Look for end of type
				if (character == '/') {
					// Separator between type/sub-type
					typeSeparatorPosition = index;
					state = ParseState.SUB_TYPE;
				}
				break;

			case SUB_TYPE:
				// Determine if terminated by space
				if (isWhiteSpace(character)) {
					// Ensure not multiple spaces
					if (subTypeEnd == -1) {
						subTypeEnd = index;
					}
				} else if (character == ';') {
					// Starting parameter
					if (subTypeEnd == -1) {
						subTypeEnd = index;
					}
					state = ParseState.PARAMETER_START;
				} else if (character == ',') {
					// No parameters for accept type
					if (subTypeEnd == -1) {
						subTypeEnd = index;
					}

					// Start new accept
					state = ParseState.NEW_ACCEPT;
				}
				break;

			case PARAMETER_START:
				// Ignore leading space
				if (isWhiteSpace(character)) {
					continue NEXT_CHARACTER;
				}

				// Start of parameter name
				paramStart = index;
				paramEnd = -1; // reset to find
				isQ = false; // reset to determine
				state = ParseState.PARAMETER_NAME;
				break;

			case PARAMETER_NAME:
				// Determine if terminated by space
				isParamEnd = false;
				if (isWhiteSpace(character)) {
					// Ensure not multiple spaces
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}

				} else if (character == '=') {
					// Parameter with value
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}
					state = ParseState.PARAMETER_VALUE_START;

				} else if (character == ';') {
					// Parameter without value
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}
					state = ParseState.PARAMETER_START;

				} else if (character == ',') {
					// Parameter without value, and no more parameters
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}
					state = ParseState.NEW_ACCEPT;
				}
				if (isParamEnd) {
					// Have another parameter
					parameterCount++;

					// Found end of parameter name, so determine if q
					if ((paramEnd - paramStart) == 1) { // "q"
						isQ = accept.charAt(paramStart) == 'q';
					}
				}
				break;

			case PARAMETER_VALUE_START:
				// Ignore leading space
				if (isWhiteSpace(character)) {
					continue NEXT_CHARACTER;
				}

				// Start of parameter name
				paramStart = index;
				paramEnd = -1; // reset to find
				state = ParseState.PARAMETER_VALUE;
				break;

			case PARAMETER_VALUE:
				// Determine if terminated by space
				isParamEnd = false;
				if (isWhiteSpace(character)) {
					// Ensure not multiple spaces
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}

				} else if (character == ';') {
					// Another parameter
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}
					state = ParseState.PARAMETER_START;

				} else if (character == ',') {
					// No more parameters
					if (paramEnd == -1) {
						paramEnd = index;
						isParamEnd = true;
					}
					state = ParseState.NEW_ACCEPT;
				}
				if (isParamEnd && isQ) {
					// Found q value
					q = accept.substring(paramStart, paramEnd);
					if (q.length() == 0) {
						// No value, so assume lowest
						q = "0";
					} else if (q.charAt(0) == '.') {
						// Prefix with 0 to allow string sorting
						q = "0" + q;
					}
				}
				break;
			}
		}

		// Handle reached end of accept value
		switch (state) {
		case NEW_ACCEPT:
		case TYPE:
			break;
		case SUB_TYPE:
			// Load the default accept type
			head = loadAcceptType(accept, typeStart, typeSeparatorPosition, accept.length(), "0", 0, head);
			break;
		case PARAMETER_NAME:
			parameterCount++; // include last parameter
			// carry on to load accept type
			
		case PARAMETER_START:
		case PARAMETER_VALUE_START:
			// Just parameter name, so no check for q ending parameter
			head = loadAcceptType(accept, typeStart, typeSeparatorPosition, subTypeEnd, q, parameterCount, head);
			break;
		case PARAMETER_VALUE:
			// Check if last parameter is q
			if ((paramEnd == -1) && isQ) {
				q = accept.substring(paramStart, accept.length());
			}
			head = loadAcceptType(accept, typeStart, typeSeparatorPosition, subTypeEnd, q, parameterCount, head);
			break;
		}

		return head;
	}

	/**
	 * Loads the {@link AcceptType} to the linked list, returning the head of
	 * the linked list.
	 * 
	 * @param accept
	 *            <code>accept</code> {@link HttpHeader} value.
	 * @param typeStart
	 *            Start of type.
	 * @param typeSeparatorPosition
	 *            Position of / separating type and sub-type.
	 * @param subTypeEnd
	 *            End of sub-type.
	 * @param q
	 *            <code>q</code> value.
	 * @param parameterCount
	 *            Number of parameters.
	 * @param head
	 *            Previous head {@link AcceptType} of the linked list.
	 * @return Potentially new head {@link AcceptType} of the linked list.
	 */
	private static final AcceptType loadAcceptType(String accept, int typeStart, int typeSeparatorPosition,
			int subTypeEnd, String q, int parameterCount, AcceptType head) {

		// Determine if wild card match
		if ((subTypeEnd - typeStart) == 3) { // "*/*"
			// Potentially wild card match
			boolean isTypeWild = accept.charAt(typeStart) == '*';
			boolean isSubTypeWild = accept.charAt(subTypeEnd - 1) == '*';
			if (isTypeWild && isSubTypeWild) {
				// Accept any content type
				return appendAcceptType(head, new AnyAcceptType(q, parameterCount));
			} else if (isSubTypeWild) {
				// Accept specific type and any sub type
				String typePrefix = accept.substring(typeStart, typeSeparatorPosition);
				return appendAcceptType(head, new TypeAcceptType(typePrefix, q, parameterCount));
			}

		} else if ((subTypeEnd - (typeSeparatorPosition + 1)) == 1) { // "*"
			// Potentially sub type wild card match
			boolean isSubTypeWild = accept.charAt(subTypeEnd - 1) == '*';
			if (isSubTypeWild) {
				// Accept specific type and any sub type (+1 to include /)
				String typePrefix = accept.substring(typeStart, (typeSeparatorPosition + 1));
				return appendAcceptType(head, new TypeAcceptType(typePrefix, q, parameterCount));
			}
		}

		// Accept specific type and specific sub type
		String contentType = accept.substring(typeStart, subTypeEnd);
		return appendAcceptType(head, new SubTypeAcceptType(contentType, q, parameterCount));
	}

	/**
	 * Appends the {@link AcceptType} into the linked list.
	 * 
	 * @param head
	 *            Previous head {@link AcceptType} of the linked list.
	 * @param newAccept
	 *            {@link AcceptType} to add.
	 * @return Potentially new head {@link AcceptType} of the linked list.
	 */
	private static final AcceptType appendAcceptType(AcceptType head, AcceptType newAccept) {

		// Determine if head
		if (head == null) {
			return newAccept; // only entry in list
		}

		// Determine if should be head
		if (head.compare(newAccept) < 0) {
			// Accept is to be new head
			newAccept.next = head;
			return newAccept;
		}

		// Insert somewhere in the list
		AcceptType current = head;
		while (current.next != null) {

			// Determine if should come before next value
			if (current.next.compare(newAccept) < 0) {
				// Insert before next value
				newAccept.next = current.next;
				current.next = newAccept;
				return head; // inserted
			}

			// Move to next position
			current = current.next;
		}

		// As here, did not insert, so append to list
		current.next = newAccept;
		return head;
	}

	/**
	 * Abstract <code>accept</code> <code>content-type</code> value from the
	 * {@link HttpRequest}.
	 */
	private static abstract class AcceptType {

		/**
		 * <code>q</code> value. Used for sorting results.
		 */
		private String q;

		/**
		 * Weight of wild card. Used for sorting results, with:
		 * <ul>
		 * <li><code>0</code>: * /*</li>
		 * <li><code>1</code>: content\/*</li>
		 * <li><code>2</code>: content/type</li>
		 * </ul>
		 */
		private int wildcardWeight;

		/**
		 * Number of parameters. Used for sorting results.
		 */
		private int parameterCount;

		/**
		 * Next {@link AcceptType}.
		 */
		private AcceptType next = null;

		/**
		 * Instantiate.
		 * 
		 * @param q
		 *            <code>q</code> value.
		 * @param wildcardWeight
		 *            Wild card weight.
		 * @param parameterCount
		 *            Parameter count.
		 */
		protected AcceptType(String q, int wildcardWeight, int parameterCount) {
			this.q = q;
			this.wildcardWeight = wildcardWeight;
			this.parameterCount = parameterCount;
		}

		/**
		 * Indicates if matches the <code>content-type</code>.
		 * 
		 * @param contentType
		 *            <code>content-type</code>.
		 * @return <code>true</code> if matches the <code>content-type</code>.
		 */
		protected abstract boolean isMatch(String contentType);

		/**
		 * Compares this against another {@link AcceptType}.
		 * 
		 * @param other
		 *            Other {@link AcceptType}.
		 * @return Compare -X / 0 / +X based on lesser, equal or greater
		 *         matching weight.
		 */
		private int compare(AcceptType other) {

			// Compare first on 'q' value
			int compare = this.q.compareTo(other.q);
			if (compare != 0) {
				return compare;
			}

			// Next compare on wild card weight
			compare = this.wildcardWeight - other.wildcardWeight;
			if (compare != 0) {
				return compare;
			}

			// Next compare on parameter count
			compare = this.parameterCount - other.parameterCount;
			if (compare != 0) {
				return compare;
			}

			// As here, equal in sorting weight
			return 0;
		}
	}

	/**
	 * {@link AcceptType} for * /*.
	 */
	private static class AnyAcceptType extends AcceptType {

		/**
		 * Instantiate.
		 * 
		 * @param q
		 *            <code>q</code> value.
		 * @param parameterCount
		 *            Parmeter count.
		 */
		protected AnyAcceptType(String q, int parameterCount) {
			super(q, 0, parameterCount);
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected boolean isMatch(String contentType) {
			// Matches any content type
			return true;
		}
	}

	/**
	 * {@link AcceptType} for type/*.
	 */
	private static class TypeAcceptType extends AcceptType {

		/**
		 * <code>content-type</code> prefix.
		 */
		private final String contentPrefix;

		/**
		 * Instantiate.
		 * 
		 * @param contentPrefix
		 *            <code>content-type</code> prefix.
		 * @param q
		 *            <code>q</code> value.
		 * @param parameterCount
		 *            Parmeter count.
		 */
		protected TypeAcceptType(String contentPrefix, String q, int parameterCount) {
			super(q, 1, parameterCount);
			this.contentPrefix = contentPrefix;
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected boolean isMatch(String contentType) {
			return contentType.startsWith(this.contentPrefix);
		}
	}

	/**
	 * {@link AcceptType} for type/subtype.
	 */
	private static class SubTypeAcceptType extends AcceptType {

		/**
		 * <code>content-type</code>.
		 */
		private final String contentType;

		/**
		 * Instantiate.
		 * 
		 * @param contentType
		 *            <code>content-type</code>.
		 * @param q
		 *            <code>q</code> value.
		 * @param parameterCount
		 *            Parmeter count.
		 */
		protected SubTypeAcceptType(String contentType, String q, int parameterCount) {
			super(q, 2, parameterCount);
			this.contentType = contentType;
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected boolean isMatch(String contentType) {
			return this.contentType.equals(contentType);
		}
	}

}