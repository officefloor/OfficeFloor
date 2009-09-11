/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Utility functions for a {@link HttpFile}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileUtil {

	/**
	 * Transforms the input Request URI path to a canonical path.
	 *
	 * @param path
	 *            Path to transform.
	 * @return Canonical path.
	 */
	public static String transformToCanonicalPath(String path) {

		// Determine if empty path
		if (path == null) {
			return null; // empty path so not canonical
		}
		path = path.trim(); // remove leading/trailing white space
		if (path.length() == 0) {
			return null; // empty path so not canonical
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

		// Flag to determine if path is canonical (pass by ref)
		boolean[] isPathCanonical = new boolean[] { true };

		// Iterate over the path creating canonical path
		int segmentBegin = -1;
		PARSE_PATH: for (int i = 0; i < path.length(); i++) {
			char character = path.charAt(i);

			switch (character) {
			case '/':
				// Determine if previous segment
				if (segmentBegin >= 0) {
					// Process the segment
					if (!processSegment(path, segmentBegin, i,
							canonicalSegments, isPathCanonical)) {
						// Not valid canonical path
						return null;
					}
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
			// Process the last segment
			if (!processSegment(path, segmentBegin, path.length(),
					canonicalSegments, isPathCanonical)) {
				// Not valid canonical path
				return null;
			}
		}

		// Determine if path is already canonical
		if (isPathCanonical[0]) {
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
	 * @return <code>true</code> if segment results in valid canonical path.
	 */
	private static boolean processSegment(String path, int beginIndex,
			int endIndex, Deque<String> canonicalSegments,
			boolean[] isPathCanonical) {

		// Obtain the segment
		String segment = path.substring(beginIndex, endIndex);

		// Handle segment
		if ((segment.length() == 0) || (".".equals(segment))) {
			// Do not include and path is not canonical
			isPathCanonical[0] = false;

		} else if ("..".equals(segment)) {
			// Determine if tail segment to remove
			if (canonicalSegments.size() == 0) {
				return false; // invalid canonical path
			}

			// Valid so equate result
			canonicalSegments.removeLast();

			// Path not canonical
			isPathCanonical[0] = false;

		} else {
			// Canonical path segment
			canonicalSegments.add(segment);
		}

		// Valid segment if here
		return true;
	}

	/**
	 * All access via static methods.
	 */
	private HttpFileUtil() {
	}

}