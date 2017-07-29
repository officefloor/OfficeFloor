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
package net.officefloor.plugin.web.http.location;

import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource.Dependencies;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.protocol.HttpStatus;

/**
 * {@link HttpApplicationLocation} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationLocationMangedObject
		implements CoordinatingManagedObject<Dependencies>, HttpApplicationLocation {

	/**
	 * Transforms the input Request URI path to a canonical path.
	 * 
	 * @param path
	 *            Path to transform.
	 * @return Canonical path.
	 * @throws InvalidHttpRequestUriException
	 *             Should the Request URI path be invalid.
	 */
	public static String transformToCanonicalPath(String path) throws InvalidHttpRequestUriException {

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
	 * @throws InvalidHttpRequestUriException
	 *             Should the segment result in an invalid path.
	 */
	private static boolean processSegment(String path, int beginIndex, int endIndex, Deque<String> canonicalSegments)
			throws InvalidHttpRequestUriException {

		// Obtain the segment
		String segment = path.substring(beginIndex, endIndex);

		// Handle segment
		if ((segment.length() == 0) || (".".equals(segment))) {
			// Do not include and path is not canonical
			return false;

		} else if ("..".equals(segment)) {
			// Must have tail segment to remove
			if (canonicalSegments.size() == 0) {
				throw createInvalidHttpRequestUriException(path);
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
	 * Creates an {@link InvalidHttpRequestUriException}.
	 * 
	 * @param path
	 *            Path that is invalid.
	 * @return {@link InvalidHttpRequestUriException}.
	 */
	private static InvalidHttpRequestUriException createInvalidHttpRequestUriException(String path) {
		return new InvalidHttpRequestUriException(HttpStatus.SC_BAD_REQUEST, "Invalid request URI path [" + path + "]");
	}

	/**
	 * Domain.
	 */
	private final String domain;

	/**
	 * HTTP port.
	 */
	private final int httpPort;

	/**
	 * HTTPS port.
	 */
	private final int httpsPort;

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * Cluster host name.
	 */
	private final String clusterHostName;

	/**
	 * Cluster HTTP port.
	 */
	private final int clusterHttpPort;

	/**
	 * Cluster HTTPS port.
	 */
	private final int clusterHttpsPort;

	/**
	 * Unsecured prefix.
	 */
	private final String unsecuredPrefix;

	/**
	 * Secured prefix.
	 */
	private final String securedPrefix;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection connection;

	/**
	 * Initiate.
	 * 
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @param contextPath
	 *            Context path.
	 * @param clusterHostName
	 *            Cluster host name.
	 * @param clusterHttpPort
	 *            Cluster HTTP port.
	 * @param clusterHttpsPort
	 *            Cluster HTTPS port.
	 */
	public HttpApplicationLocationMangedObject(String domain, int httpPort, int httpsPort, String contextPath,
			String clusterHostName, int clusterHttpPort, int clusterHttpsPort) {
		this.domain = domain;
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.contextPath = contextPath;
		this.clusterHostName = clusterHostName;
		this.clusterHttpPort = clusterHttpPort;
		this.clusterHttpsPort = clusterHttpsPort;

		// Determine the unsecured prefix for a client link
		this.unsecuredPrefix = "http://" + this.domain + (this.httpPort == 80 ? "" : ":" + this.httpPort)
				+ (this.contextPath == null ? "" : this.contextPath);

		// Determine the secured prefix for a client link
		this.securedPrefix = "https://" + this.domain + (this.httpsPort == 443 ? "" : ":" + this.httpsPort)
				+ (this.contextPath == null ? "" : this.contextPath);
	}

	/*
	 * =================== ManagedObject ========================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {
		// Obtain the server HTTP connection
		this.connection = (ServerHttpConnection) registry.getObject(Dependencies.SERVER_HTTP_CONNECTION);
	}

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ================ HttpApplicationLocation =================
	 */

	@Override
	public String getDomain() {
		return this.domain;
	}

	@Override
	public int getHttpPort() {
		return this.httpPort;
	}

	@Override
	public int getHttpsPort() {
		return this.httpsPort;
	}

	@Override
	public String getContextPath() {
		return this.contextPath;
	}

	@Override
	public String getClusterHostName() {
		return this.clusterHostName;
	}

	@Override
	public int getClusterHttpPort() {
		return this.clusterHttpPort;
	}

	@Override
	public int getClusterHttpsPort() {
		return this.clusterHttpsPort;
	}

	@Override
	public String transformToApplicationCanonicalPath(String requestUri)
			throws InvalidHttpRequestUriException, IncorrectHttpRequestContextPathException {

		// Root if empty path
		if (requestUri == null) {
			return "/"; // root path
		}

		// Trim path and ensure not empty/blank
		requestUri = requestUri.trim();
		if (requestUri.length() == 0) {
			return "/"; // root path
		}

		// Transform to a canonical path
		String canonicalPath = transformToCanonicalPath(requestUri);

		// Determine if need to strip off context path
		if (this.contextPath != null) {

			// Ensure have context path
			if (!(canonicalPath.startsWith(this.contextPath))) {
				throw new IncorrectHttpRequestContextPathException(HttpStatus.SC_NOT_FOUND,
						"Incorrect context path for application [context=" + this.contextPath + ", request="
								+ requestUri + "]");
			}

			// Strip off the context path
			canonicalPath = canonicalPath.substring(this.contextPath.length());
			if (canonicalPath.length() == 0) {
				canonicalPath = "/"; // root path
			}
		}

		// Return the canonical path for the application
		return canonicalPath;
	}

	@Override
	public String transformToClientPath(String applicationPath, boolean isSecure) {

		// Handle based on whether over secure connection
		if (this.connection.isSecure()) {
			// Over secure connection
			if (isSecure) {
				// Both secure so provide context prefix
				return this.prefixWithContextPath(applicationPath);
			} else {
				// Require unsecuring link
				return this.unsecuredPrefix + applicationPath;
			}

		} else {
			// Over unsecured connection
			if (!isSecure) {
				// Both unsecure so provide context prefix
				return this.prefixWithContextPath(applicationPath);
			} else {
				// Require securing link
				return this.securedPrefix + applicationPath;
			}
		}
	}

	/**
	 * Prefixes the application path with the context path.
	 * 
	 * @param applicationPath
	 *            Application path.
	 * @return Application path prefixed with the context path.
	 */
	private String prefixWithContextPath(String applicationPath) {
		if (this.contextPath == null) {
			return applicationPath;
		} else {
			return this.contextPath + applicationPath;
		}
	}

}