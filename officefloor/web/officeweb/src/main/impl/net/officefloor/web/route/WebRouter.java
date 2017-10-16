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
package net.officefloor.web.route;

import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.escalation.BadRequestHttpException;

/**
 * Routes {@link ServerHttpConnection} instances to their respective handling
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouter {

	/**
	 * <p>
	 * Transforms the path to canonical path for the application.
	 * <p>
	 * Available for routing to call directly to avoid {@link ManagedObject}
	 * creation.
	 * 
	 * @param path
	 *            Path to be transformed.
	 * @param contextPath
	 *            Context path of the application. May be <code>null</code> if
	 *            context at root.
	 * @return Canonical path for the application.
	 * @throws BadRequestHttpException
	 *             If path is invalid.
	 * @throws InvalidContextPathException
	 *             If unknown context path.
	 */
	public static String transformToApplicationCanonicalPath(String path, String contextPath)
			throws BadRequestHttpException, InvalidContextPathException {

		// Root if empty path
		if (path == null) {
			return "/"; // root path
		}

		// Trim path and ensure not empty/blank
		path = path.trim();
		if (path.length() == 0) {
			return "/"; // root path
		}

		// Transform to a canonical path
		String canonicalPath = transformToCanonicalPath(path);

		// Determine if need to strip off context path
		if (contextPath != null) {

			// Ensure have context path
			if (!(canonicalPath.startsWith(contextPath))) {
				throw new InvalidContextPathException(contextPath, path);
			}

			// Strip off the context path
			canonicalPath = canonicalPath.substring(contextPath.length());
			if (canonicalPath.length() == 0) {
				canonicalPath = "/"; // root path
			}
		}

		// Return the canonical path for the application
		return canonicalPath;
	}

	/**
	 * Transforms the input Request URI path to a canonical path.
	 * 
	 * @param path
	 *            Path to transform.
	 * @return Canonical path.
	 * @throws BadRequestHttpException
	 *             Should the Request URI path be invalid.
	 */
	public static String transformToCanonicalPath(String path) throws BadRequestHttpException {

		// Root if empty path
		if (path == null) {
			return "/"; // root path
		}

		// Trim path and ensure not empty/blank
		path = path.trim();
		if (path.length() == 0) {
			return "/"; // root path
		}

		// Determine if starting with protocol and/or domain
		if (path.charAt(0) != '/') {
			// Find first single '/' sequence to remove protocol/domain
			int singleSlashBegin = -1;
			int slashSequenceCount = 0;
			SINGLE_SLASH_SEARCH: for (int i = 0; i < path.length(); i++) {
				char character = path.charAt(i);
				switch (character) {
				case '/':
					// Increment sequence count of '/'
					slashSequenceCount++;
					break;

				default:
					// Determine if previous char was single '/' sequence
					if (slashSequenceCount == 1) {
						// Previous char was first single '/' sequence
						singleSlashBegin = i - 1; // previous char
						break SINGLE_SLASH_SEARCH; // found first single '/'
					}

					// No '/' in sequence
					slashSequenceCount = 0;
					break;
				}
			}

			// Determine if single '/' sequence
			if (singleSlashBegin < 0) {
				// Just protocol/domain name
				return "/"; // return absolute path
			} else {
				// Strip off the leading protocol/domain
				path = path.substring(singleSlashBegin);
			}
		}

		// Create list to determine canonical path
		Deque<String> canonicalSegments = new LinkedList<String>();

		// Flag to determine if path is canonical (by default canonical)
		boolean isPathCanonical = true;

		// Iterate over the path creating canonical path
		int segmentBegin = -1;
		PARSE_PATH: for (int i = 0; i < path.length(); i++) {
			char character = path.charAt(i);

			switch (character) {
			case '/':
				// Determine if previous segment
				if (segmentBegin >= 0) {
					// Process the segment (keeping track if canonical)
					isPathCanonical &= processSegment(path, segmentBegin, i, canonicalSegments);
				}

				// Flag start of next segment
				segmentBegin = i + 1; // after '/'
				break;

			case '?':
			case '#':
				// Parameters/fragment so no further path
				if (segmentBegin < 0) {
					// Return root path, as only parameters/fragment
					return "/";
				} else {
					// Strip parameters/fragments from the path
					path = path.substring(0, i);
					break PARSE_PATH; // parsed all segments of path
				}
			}
		}

		// Determine if last segment
		if (segmentBegin <= path.length()) {
			// Process the last segment (keeping track if canonical)
			isPathCanonical &= processSegment(path, segmentBegin, path.length(), canonicalSegments);
		}

		// Determine if path is already canonical
		if (isPathCanonical) {
			// Already canonical so return path as is
			return path;

		} else {
			// Construct the canonical path
			if (canonicalSegments.size() == 0) {
				// No segments, so root path
				return "/";

			} else {
				// Construct the segments into the canonical path
				StringBuilder canonicalPath = new StringBuilder(path.length());
				for (String segment : canonicalSegments) {
					canonicalPath.append('/');
					canonicalPath.append(segment);
				}

				// Return the canonical path
				return canonicalPath.toString();
			}
		}
	}

	/**
	 * Processes the segment of the path.
	 * 
	 * @param path
	 *            Path.
	 * @param beginIndex
	 *            Index of beginning of segment within the path.
	 * @param endIndex
	 *            Index of end of segment within the path.
	 * @param canonicalSegments
	 *            List of segments making up the canonical path.
	 * @return Flag indicating if the path continues to be canonical for the
	 *         segment. <code>false</code> indicates the path is not canonical
	 *         and must be constructed from the resulting segments to be
	 *         canonical.
	 * @throws BadRequestHttpException
	 *             Should the segment result in an invalid path.
	 */
	private static boolean processSegment(String path, int beginIndex, int endIndex, Deque<String> canonicalSegments)
			throws BadRequestHttpException {

		// Obtain the segment
		String segment = path.substring(beginIndex, endIndex);

		// Handle segment
		if ((segment.length() == 0) || (".".equals(segment))) {
			// Do not include and path is not canonical
			return false;

		} else if ("..".equals(segment)) {
			// Must have tail segment to remove
			if (canonicalSegments.size() == 0) {
				throw new BadRequestHttpException(null, "Invalid request URI path " + path);
			}

			// Valid so equate result (remove tail segment)
			canonicalSegments.removeLast();

			// Path not canonical
			return false;

		} else {
			// Canonical path segment
			canonicalSegments.add(segment);
		}

		// Path continues to be canonical for the segment
		return true;
	}

	/**
	 * Root {@link WebRouteNode} instances.
	 */
	private final WebRouteNode[] nodes;

	/**
	 * Instantiate.
	 * 
	 * @param nodes
	 *            Root {@link WebRouteNode} instances.
	 */
	public WebRouter(WebRouteNode[] nodes) {
		this.nodes = nodes;
	}

	/**
	 * Services the {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @param managedFunctionContext
	 *            {@link ManagedFunctionContext}.
	 * @return <code>true</code> if {@link HttpRequest} was routed to a
	 *         {@link WebRouteHandler}. <code>false</code> indicates not
	 *         handled.
	 */
	public boolean service(HttpRequest request, ManagedFunctionContext<?, Indexed> managedFunctionContext) {

		// Obtain the request details
		HttpMethod method = request.getHttpMethod();
		String requestUri = request.getRequestURI();

		// Attempt to match to a route
		for (int i = 0; i < this.nodes.length; i++) {
			WebRouteNode node = this.nodes[i];

			// Determine if handled by node
			if (node.handle(method, requestUri, 0, null, managedFunctionContext)) {
				return true; // servicing request
			}
		}

		// As here, not handling request
		return false;
	}

}