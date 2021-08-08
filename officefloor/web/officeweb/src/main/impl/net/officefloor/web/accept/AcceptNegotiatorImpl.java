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

package net.officefloor.web.accept;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link AcceptNegotiator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AcceptNegotiatorImpl<H> implements AcceptNegotiator<H> {

	/**
	 * Accept handler.
	 */
	public static class AcceptHandler<H> {

		/**
		 * Type of {@link AcceptHandler}.
		 */
		private final AcceptHandlerEnum type;

		/**
		 * <code>Content-Type</code> for matching. Value specific to
		 * {@link AcceptHandlerEnum}.
		 */
		private final String matchContentType;

		/**
		 * Handler.
		 */
		private final H handler;

		/**
		 * Instantiate.
		 * 
		 * @param handler
		 *            Handler.
		 */
		private AcceptHandler(AcceptHandlerEnum type, String matchContentType, H handler) {
			this.type = type;
			this.matchContentType = matchContentType;
			this.handler = handler;
		}
	}

	/**
	 * Easy look up of
	 * 
	 *
	 * @author Daniel Sagenschneider
	 */
	private static enum AcceptHandlerEnum {
		SUB_TYPE, TYPE, ANY
	}

	/**
	 * Creates the {@link AcceptHandler}.
	 * 
	 * @param <H>
	 *            Handle type.
	 * @param contentType
	 *            <code>Content-Type</code>
	 * @param handler
	 *            Handler.
	 * @return {@link AcceptHandler}.
	 */
	public static <H> AcceptHandler<H> createAcceptHandler(String contentType, H handler) {

		// Clean content type
		contentType = contentType.trim();

		// Determine if default content type
		if ("*/*".equals(contentType)) {
			return new AcceptHandler<H>(AcceptHandlerEnum.ANY, null, handler);
		}

		// Determine if type (with wildcard sub type)
		if (contentType.endsWith("/*")) {
			return new AcceptHandler<H>(AcceptHandlerEnum.TYPE, contentType.split("/")[0] + "/", handler);
		}

		// As here, is specific type
		return new AcceptHandler<H>(AcceptHandlerEnum.SUB_TYPE, contentType, handler);
	}

	/**
	 * {@link AcceptType} linked list to use should there be no <code>accept</code>
	 * {@link HttpHeader} values.
	 */
	private static final AcceptType MATCH_ANY = new AnyAcceptType("1", 0);

	/**
	 * {@link AcceptHandler} instances.
	 */
	private final AcceptHandler<H>[] acceptHandlers;

	/**
	 * Default {@link AcceptHandler}.
	 */
	private final AcceptHandler<H> defaultAcceptHandler;

	/**
	 * Instantiate.
	 * 
	 * @param acceptHandlers
	 *            {@link AcceptHandler} instances.
	 */
	@SuppressWarnings("unchecked")
	public AcceptNegotiatorImpl(AcceptHandler<H>[] acceptHandlers) {

		// Split into lists
		AcceptHandler<H> defaultAcceptHandler = null;
		List<AcceptHandler<H>> handlers = new ArrayList<>(acceptHandlers.length);
		for (AcceptHandler<H> handler : acceptHandlers) {
			switch (handler.type) {
			case ANY:
				// Only one default matcher allowed
				if (defaultAcceptHandler != null) {
					throw new IllegalStateException("Two default (*/*) handlers configured");
				}
				defaultAcceptHandler = handler;
				break;

			default:
				// Include remaining
				handlers.add(handler);
				break;
			}
		}

		// Sort the accept handlers
		handlers.sort((a, b) -> {
			int comparison = a.type.ordinal() - b.type.ordinal();
			if (comparison == 0) {
				// Match, so sort by content type (descending)
				return a.matchContentType.compareTo(b.matchContentType) * -1;
			}
			return comparison;
		});

		// Configure
		this.acceptHandlers = handlers.toArray(new AcceptHandler[handlers.size()]);
		this.defaultAcceptHandler = defaultAcceptHandler;
	}

	/*
	 * ================== AcceptNegotiator ====================
	 */

	@Override
	public H getHandler(HttpRequest request) {

		// Parse out the accept type
		AcceptType acceptType = parseAccept(request);

		// Find first matching handler
		while (acceptType != null) {

			// Attempt to match to accept handler
			for (int i = 0; i < this.acceptHandlers.length; i++) {
				AcceptHandler<H> handler = this.acceptHandlers[i];
				if (acceptType.isMatch(handler)) {

					// Found handler
					return handler.handler;
				}
			}

			// Try next accept type
			acceptType = acceptType.next;
		}

		// Determine if default match
		if (this.defaultAcceptHandler != null) {
			return this.defaultAcceptHandler.handler;
		}

		// As here, no match found
		return null;
	}

	/**
	 * Parses the {@link AcceptType} linked list from the
	 * {@link ServerHttpConnection}.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @return Head {@link AcceptType} of the linked list.
	 */
	private static AcceptType parseAccept(HttpRequest request) {

		// Accept type
		AcceptType head = null;

		// Load the accept types
		HttpRequestHeaders headers = request.getHeaders();
		for (HttpHeader header : headers.getHeaders("accept")) {
			head = parseAccept(header.getValue(), head);
		}

		// Determine if only wild card match
		// - no head, so will match any type
		// - only one head that is any match, so will match any type
		boolean isOnlyWildcard = ((head == null) || ((head.next == null) && (head.getClass() == AnyAcceptType.class)));

		// Default to content-type if wild card only
		if (isOnlyWildcard) {

			// Attempt to match first on input content type
			// (e.g. if JSON sent then respond with JSON)
			HttpHeader contentTypeHeader = headers.getHeader("content-type");
			if (contentTypeHeader != null) {
				head = new SubTypeAcceptType(contentTypeHeader.getValue(), "1", 0);
			}

			// Now match any
			if (head == null) {
				head = MATCH_ANY;
			} else {
				head.next = MATCH_ANY;
			}
		}

		// Return the head of the linked list
		return head;
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
	 * Parses the <code>accept</code> {@link HttpHeader} value returning the head
	 * {@link AcceptType} of the linked list of {@link AcceptType} instances.
	 * 
	 * @param accept
	 *            <code>accept</code> {@link HttpHeader} value.
	 * @param head
	 *            Head {@link AcceptType} from another
	 *            <code>accept<code> {@link HttpHeader} should there be multiple <code>accept</code>
	 *            {@link HttpHeader} values. Will be <code>null</code> if no other
	 *            <code>accept</code> {@link HttpHeader}.
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
	 * Loads the {@link AcceptType} to the linked list, returning the head of the
	 * linked list.
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
		 * Indicates if matches the {@link AcceptHandler}.
		 * 
		 * @param acceptHandler
		 *            {@link AcceptHandler}.
		 * @return <code>true</code> if matches the {@link AcceptHandler}.
		 */
		protected abstract <H> boolean isMatch(AcceptHandler<H> acceptHandler);

		/**
		 * Compares this against another {@link AcceptType}.
		 * 
		 * @param other
		 *            Other {@link AcceptType}.
		 * @return Compare -X / 0 / +X based on lesser, equal or greater matching
		 *         weight.
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
		 *            Parameter count.
		 */
		protected AnyAcceptType(String q, int parameterCount) {
			super(q, 0, parameterCount);
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected <H> boolean isMatch(AcceptHandler<H> acceptHandler) {
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
		 *            Parameter count.
		 */
		protected TypeAcceptType(String contentPrefix, String q, int parameterCount) {
			super(q, 1, parameterCount);
			this.contentPrefix = contentPrefix;
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected <H> boolean isMatch(AcceptHandler<H> acceptHandler) {
			switch (acceptHandler.type) {
			case SUB_TYPE:
				return acceptHandler.matchContentType.startsWith(this.contentPrefix);

			case TYPE:
				return acceptHandler.matchContentType.equals(this.contentPrefix);

			case ANY:
				return true;

			default:
				throw new IllegalStateException(
						"Unknown " + AcceptHandlerEnum.class.getName() + " type " + acceptHandler.type);
			}
		}
	}

	/**
	 * {@link AcceptType} for type/sub-type.
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
		 *            Parameter count.
		 */
		protected SubTypeAcceptType(String contentType, String q, int parameterCount) {
			super(q, 2, parameterCount);
			this.contentType = contentType;
		}

		/*
		 * =============== AcceptType ===============
		 */

		@Override
		protected <H> boolean isMatch(AcceptHandler<H> acceptHandler) {
			switch (acceptHandler.type) {
			case SUB_TYPE:
				return this.contentType.equals(acceptHandler.matchContentType);

			case TYPE:
				return this.contentType.startsWith(acceptHandler.matchContentType);

			case ANY:
				return true;

			default:
				throw new IllegalStateException(
						"Unknown " + AcceptHandlerEnum.class.getName() + " type " + acceptHandler.type);
			}
		}
	}

}
