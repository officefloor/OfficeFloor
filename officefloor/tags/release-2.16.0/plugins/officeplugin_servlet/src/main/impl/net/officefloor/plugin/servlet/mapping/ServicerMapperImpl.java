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
package net.officefloor.plugin.servlet.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenHandler;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniser;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

/**
 * {@link ServicerMapper} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicerMapperImpl implements ServicerMapper {

	/**
	 * {@link HttpRequestTokeniser}.
	 */
	private static HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();

	/**
	 * Path separator.
	 */
	private static final char PATH_SEPARATOR_CHAR = '/';

	/**
	 * Path separator.
	 */
	private static final String PATH_SEPARATOR = new String(
			new char[] { PATH_SEPARATOR_CHAR });

	/**
	 * Wild card.
	 */
	private static final String WILDCARD = "*";

	/**
	 * {@link ExactMapper} priority.
	 */
	private static final int PRIORITY_EXACT = 0;

	/**
	 * {@link PathMapper} priority.
	 */
	private static final int PRIORITY_PATH = 1;

	/**
	 * {@link ExtensionMapper} priority.
	 */
	private static final int PRIORITY_EXTENSION = 2;

	/**
	 * {@link DefaultMapper} priority.
	 */
	private static final int PRIORITY_DEFAULT = 3;

	/**
	 * {@link HttpServletServicer} instances by their name.
	 */
	private final Map<String, HttpServletServicer> namedServicers = new HashMap<String, HttpServletServicer>();

	/**
	 * {@link Mapper} instances indexed by priority.
	 */
	private final Mapper[] mappers = new Mapper[4];

	/**
	 * Initiate.
	 * 
	 * @param servicers
	 *            {@link HttpServletServicer} instances for this
	 *            {@link ServicerMapper}.
	 */
	public ServicerMapperImpl(HttpServletServicer... servicers) {

		// Loop over servicers loading their mappings
		for (HttpServletServicer servicer : servicers) {

			// Load by servlet name
			String servletName = servicer.getServletName();
			if (servletName != null) {
				this.namedServicers.put(servletName, servicer);
			}

			// Obtain the servlet mappings
			String[] expressions = servicer.getServletMappings();
			if (expressions == null) {
				continue; // no mappings so ignore servicer
			}

			// Load the servlet mappings
			for (String expression : expressions) {

				// Load based on mapping type
				Mapper mapper;
				if (PATH_SEPARATOR.equals(expression)) {
					// Default mapping
					mapper = this.getMapper(PRIORITY_DEFAULT);
				} else if (expression.endsWith(WILDCARD)) {
					// Path mapping
					mapper = this.getMapper(PRIORITY_PATH);
				} else if (expression.startsWith(WILDCARD)) {
					// Extension mapping
					mapper = this.getMapper(PRIORITY_EXTENSION);
				} else {
					// Exact mapping
					mapper = this.getMapper(PRIORITY_EXACT);
				}
				mapper.loadServicer(expression, servicer);
			}
		}
	}

	/**
	 * Obtains the {@link Mapper}.
	 * 
	 * @param priority
	 *            Priority to determine the type of {@link Mapper}.
	 * @return {@link Mapper} for the priority.
	 */
	private Mapper getMapper(int priority) {

		// Lazy create the mapper
		Mapper mapper = this.mappers[priority];
		if (mapper == null) {
			switch (priority) {
			case PRIORITY_EXACT:
				mapper = new ExactMapper();
				break;
			case PRIORITY_PATH:
				mapper = new PathMapper();
				break;
			case PRIORITY_EXTENSION:
				mapper = new ExtensionMapper();
				break;
			case PRIORITY_DEFAULT:
				mapper = new DefaultMapper();
				break;
			}
			this.mappers[priority] = mapper;
		}

		// Return the mapper
		return mapper;
	}

	/*
	 * ======================= ServicerMapper =====================
	 */

	@Override
	public ServicerMapping mapPath(String path) {

		// Create mapping from path
		Mapping mapping;
		try {
			mapping = new Mapping(path);
		} catch (HttpRequestTokeniseException ex) {
			// Failed tokenising path so no mapping
			return null;
		}

		// Iterate over mappers to map path
		for (int i = 0; i < this.mappers.length; i++) {

			// Ensure mapper available for priority
			Mapper mapper = this.mappers[i];
			if (mapper == null) {
				continue;
			}

			// Attempt to map
			ServicerMapping servicerMapping = mapper.mapPath(mapping);
			if (servicerMapping != null) {
				return servicerMapping; // found mapping
			}
		}

		// As here no servicer mapping
		return null;
	}

	@Override
	public HttpServletServicer mapName(String name) {
		return this.namedServicers.get(name);
	}

	/**
	 * Mapping.
	 */
	private static class Mapping implements HttpRequestTokenHandler {

		/**
		 * Path.
		 */
		private String path = "";

		/**
		 * Parameters.
		 */
		private final Map<String, String[]> parameters = new HashMap<String, String[]>();

		/**
		 * Query String.
		 */
		private String queryString = null;

		/**
		 * Initiate.
		 * 
		 * @param expression
		 *            Mapping expression.
		 * @throws HttpRequestTokeniseException
		 *             If fails to tokenise expression.
		 */
		public Mapping(String expression) throws HttpRequestTokeniseException {
			tokeniser.tokeniseRequestURI(expression, this);
		}

		/**
		 * Obtains the path.
		 * 
		 * @return Path.
		 */
		public String getPath() {
			return this.path;
		}

		/**
		 * Creates the {@link ServicerMapping}.
		 * 
		 * @param servicerPath
		 *            {@link HttpServletServicer} path.
		 * @param pathInfo
		 *            Path info.
		 * @param servicer
		 *            {@link HttpServletServicer}.
		 * @return {@link ServicerMapping}.
		 */
		public ServicerMapping createServicerMapping(String servicerPath,
				String pathInfo, HttpServletServicer servicer) {
			return new ServicerMappingImpl(servicer, servicerPath, pathInfo,
					this.queryString, this.parameters);
		}

		/*
		 * ================== HttpRequestTokenHandler ==================
		 */

		@Override
		public void handlePath(String path) throws HttpRequestTokeniseException {
			this.path = path;
		}

		@Override
		public void handleHttpParameter(String name, String value)
				throws HttpRequestTokeniseException {

			// Add the parameter
			String[] values = this.parameters.get(name);
			if (values == null) {
				// First value
				values = new String[] { value };
			} else {
				// Append value
				String[] appendedValues = new String[values.length + 1];
				System.arraycopy(values, 0, appendedValues, 0, values.length);
				appendedValues[values.length] = value;
				values = appendedValues;
			}

			// Update values in map
			this.parameters.put(name, values);
		}

		@Override
		public void handleQueryString(String queryString)
				throws HttpRequestTokeniseException {
			this.queryString = queryString;
		}

		@Override
		public void handleFragment(String fragment)
				throws HttpRequestTokeniseException {
			// Ignore fragment
		}
	}

	/**
	 * Generic {@link Mapper}.
	 */
	private static interface Mapper {

		/**
		 * Obtains the priority for this {@link Mapper}.
		 * 
		 * @return Priority for this {@link Mapper}.
		 */
		int getPriority();

		/**
		 * Loads the {@link HttpServletServicer}.
		 * 
		 * @param mapping
		 *            Mapping expression.
		 * @param servicer
		 *            {@link HttpServletServicer} for the mapping.
		 */
		void loadServicer(String mapping, HttpServletServicer servicer);

		/**
		 * Matches best {@link ServicerMapper} or <code>null</code> if none
		 * appropriate.
		 * 
		 * @param mapping
		 *            {@link Mapping}.
		 * @return {@link ServicerMapper} or <code>null</code>.
		 */
		ServicerMapping mapPath(Mapping mapping);

		/**
		 * Matches all {@link HttpServletServicer} instances loading them into
		 * the {@link HttpServletServicer} listing.
		 * 
		 * @param mapping
		 *            {@link Mapping}.
		 * @param servicers
		 *            Listing of {@link HttpServletServicer} instances to be
		 *            loaded with matched {@link HttpServletServicer} instances.
		 */
		void mapAll(Mapping mapping, List<HttpServletServicer> servicers);
	}

	/**
	 * Exact match {@link Mapper}.
	 */
	private static class ExactMapper implements Mapper {

		/**
		 * Exact mappings.
		 */
		private final Map<String, HttpServletServicer> mappings = new HashMap<String, HttpServletServicer>();

		/*
		 * ==================== Mapper ======================
		 */

		@Override
		public int getPriority() {
			return PRIORITY_EXACT;
		}

		@Override
		public void loadServicer(String mapping, HttpServletServicer servicer) {
			this.mappings.put(mapping, servicer);
		}

		@Override
		public ServicerMapping mapPath(Mapping mapping) {
			HttpServletServicer servicer = this.mappings.get(mapping.getPath());
			if (servicer == null) {
				// No matching servicer
				return null;
			} else {
				// Exact match servicer
				return mapping.createServicerMapping(mapping.getPath(), null,
						servicer);
			}
		}

		@Override
		public void mapAll(Mapping mapping, List<HttpServletServicer> servicers) {
			HttpServletServicer servicer = this.mappings.get(mapping.getPath());
			if (servicer != null) {
				// Load the matched servicer
				servicers.add(servicer);
			}
		}
	}

	/**
	 * Path based {@link Mapper}.
	 */
	private static class PathMapper implements Mapper {

		/**
		 * Root {@link PathNode}.
		 */
		private final PathNode root = new PathNode("");

		/*
		 * =================== Mapper ==========================
		 */

		@Override
		public int getPriority() {
			return PRIORITY_PATH;
		}

		@Override
		public void loadServicer(String mapping, HttpServletServicer servicer) {

			// Remove the trailing wild card
			mapping = mapping.substring(0,
					(mapping.length() - WILDCARD.length()));

			// Remove starting separator (if starts with separator)
			if (mapping.startsWith(PATH_SEPARATOR)) {
				mapping = mapping.substring(PATH_SEPARATOR.length());
			}

			// Remove trailing separator (if ends with separator)
			if (mapping.endsWith(PATH_SEPARATOR)) {
				mapping = mapping.substring(0,
						(mapping.length() - PATH_SEPARATOR.length()));
			}

			// Obtain the path segments of the path
			String[] segments = mapping.split(PATH_SEPARATOR);

			// Load path nodes for the path segments
			PathNode node = this.root;
			for (String segment : segments) {

				// Ignore blank segments
				if (segment.length() == 0) {
					continue;
				}

				// Load the child
				node = node.loadChild(segment);
			}

			// Load servicer for leaf node (path)
			node.servicer = servicer;
		}

		@Override
		public ServicerMapping mapPath(Mapping mapping) {

			// Obtain the path
			String path = mapping.getPath();

			// Match on path (only match if have servicer)
			PathNode current = this.root;
			PathNode match = (current.servicer == null ? null : current);
			int startIndex = 0;
			int lastIndex = path.length() - 1;
			int matchIndex = startIndex;
			PARSE: for (int i = startIndex; i <= lastIndex; i++) {
				char character = path.charAt(i);

				// Determine if separator or last character
				if ((character == PATH_SEPARATOR_CHAR) || (i == lastIndex)) {

					// Ignore blank segments
					if (i == startIndex) {
						// Update start index after blank segment
						startIndex = i + 1; // after separator

						// Continue parsing ignoring blank segment
						continue PARSE;
					}

					// Determine if include current character
					int endIndex = i;
					if (character != PATH_SEPARATOR_CHAR) {
						// Include current character as not separator
						endIndex = endIndex + 1;
					}

					// Obtain the path segment
					String segment = path.substring(startIndex, endIndex);

					// Update start index for next segment
					startIndex = i + 1; // after separator

					// Obtain the path node
					current = current.getChild(segment);
					if (current == null) {
						// No further matching of path
						break PARSE;
					}

					// Update match if node has servicer
					if (current.servicer != null) {
						match = current; // longer path match
						matchIndex = startIndex;
					}
				}
			}

			// Determine if match
			if (match == null) {
				// No match, no servicer mapping
				return null;
			}

			// Obtain the path info
			String pathInfo = (matchIndex >= path.length() ? null
					: PATH_SEPARATOR + path.substring(matchIndex));

			// Create and return the servicer mapping
			return mapping.createServicerMapping(match.fullPath, pathInfo,
					match.servicer);
		}

		@Override
		public void mapAll(Mapping mapping, List<HttpServletServicer> servicers) {

			// Obtain the path
			String path = mapping.getPath();

			// Match on path
			PathNode current = this.root;
			int startIndex = 0;
			int lastIndex = path.length() - 1;
			PARSE: for (int i = startIndex; i <= lastIndex; i++) {
				char character = path.charAt(i);

				// Determine if separator or last character
				if ((character == PATH_SEPARATOR_CHAR) || (i == lastIndex)) {

					// Ignore blank segments
					if (i == startIndex) {
						// Update start index after blank segment
						startIndex = i + 1; // after separator

						// Continue parsing ignoring blank segment
						continue PARSE;
					}

					// Determine if include current character
					int endIndex = i;
					if (character != PATH_SEPARATOR_CHAR) {
						// Include current character as not separator
						endIndex = endIndex + 1;
					}

					// Obtain the path segment
					String segment = path.substring(startIndex, endIndex);

					// Update start index for next segment
					startIndex = i + 1; // after separator

					// Obtain the path node
					current = current.getChild(segment);
					if (current == null) {
						// No further matching of path
						break PARSE;
					}

					// Load servicer for node (if available)
					if (current.servicer != null) {
						servicers.add(current.servicer);
					}
				}
			}
		}
	}

	/**
	 * Node within the matching path.
	 */
	private static class PathNode {

		/**
		 * Full path to this {@link PathNode}.
		 */
		private final String fullPath;

		/**
		 * {@link PathNode} of this {@link PathNode} by their path segment.
		 */
		private final Map<String, PathNode> children = new HashMap<String, PathNode>();

		/**
		 * {@link HttpServletServicer} for this {@link PathNode}.
		 */
		public HttpServletServicer servicer = null;

		/**
		 * Initiate.
		 * 
		 * @param fullPath
		 *            Full path to this {@link PathNode}.
		 */
		public PathNode(String fullPath) {
			this.fullPath = fullPath;
		}

		/**
		 * Loads the child {@link PathNode}.
		 * 
		 * @param segment
		 *            Segment for the {@link PathNode}.
		 * @return Child {@link PathNode}.
		 */
		public PathNode loadChild(String segment) {
			// Lazy load the path
			PathNode child = this.children.get(segment);
			if (child == null) {
				child = new PathNode(this.fullPath + PATH_SEPARATOR + segment);
				this.children.put(segment, child);
			}

			// Return the child
			return child;
		}

		/**
		 * Obtains the child {@link PathNode} for the segment.
		 * 
		 * @param segment
		 *            Segment for the {@link PathNode}.
		 * @return Child {@link PathNode} or <code>null</code> if no child
		 *         {@link PathNode} by segment.
		 */
		public PathNode getChild(String segment) {
			return this.children.get(segment);
		}
	}

	/**
	 * Extension based {@link Mapper}.
	 */
	private static class ExtensionMapper implements Mapper {

		/**
		 * {@link HttpServletServicer} by its extension.
		 */
		private final Map<String, HttpServletServicer> extensionServicers = new HashMap<String, HttpServletServicer>();

		/*
		 * =================== Mapper ==========================
		 */

		@Override
		public int getPriority() {
			return PRIORITY_EXTENSION;
		}

		@Override
		public void loadServicer(String mapping, HttpServletServicer servicer) {

			// Remove the wild card from start
			mapping = mapping.substring(WILDCARD.length());

			// Remove dot for extension
			final String DOT = ".";
			if (mapping.startsWith(DOT)) {
				mapping = mapping.substring(DOT.length());
			}

			// Load in lower case for matching
			String extension = mapping.toLowerCase();

			// Load the extension mapping
			this.extensionServicers.put(extension, servicer);
		}

		@Override
		public ServicerMapping mapPath(Mapping mapping) {

			// Obtain the extension
			String extension = this.getExtension(mapping);
			if (extension == null) {
				// No extension, so no mapping
				return null;
			}

			// Obtain the servicer for extension
			HttpServletServicer servicer = this.extensionServicers
					.get(extension);
			if (servicer == null) {
				// No servicer, so no mapping
				return null;
			}

			// Return the servicer mapping (match on full path)
			return mapping.createServicerMapping(mapping.getPath(), null,
					servicer);
		}

		@Override
		public void mapAll(Mapping mapping, List<HttpServletServicer> servicers) {

			// Obtain the extension
			String extension = this.getExtension(mapping);
			if (extension == null) {
				// No extension, so no servicer
				return;
			}

			// Obtain the servicer for extension
			HttpServletServicer servicer = this.extensionServicers
					.get(extension);
			if (servicer != null) {
				// No servicer
				servicers.add(servicer);
			}
		}

		/**
		 * Obtains the extension for the {@link Mapping}.
		 * 
		 * @param mapping
		 *            {@link Mapping}.
		 * @return Extension or <code>null</code> if not extension.
		 */
		private String getExtension(Mapping mapping) {

			// Obtain the extension
			String path = mapping.getPath();
			int dotIndex = path.lastIndexOf('.');
			if (dotIndex < 0) {
				// No extension, so no mapping
				return null;
			}
			// +1 to ignore '.'
			String extension = path.substring(dotIndex + 1);
			extension = extension.toLowerCase(); // to match

			// Return the extension
			return extension;
		}
	}

	/**
	 * Default {@link Mapper}.
	 */
	private static class DefaultMapper implements Mapper {

		/**
		 * Default {@link HttpServletServicer}.
		 */
		private HttpServletServicer defaultServicer;

		/*
		 * =================== Mapper ==========================
		 */

		@Override
		public int getPriority() {
			return PRIORITY_DEFAULT;
		}

		@Override
		public void loadServicer(String mapping, HttpServletServicer servicer) {
			// Should only be the one default servicer
			this.defaultServicer = servicer;
		}

		@Override
		public ServicerMapping mapPath(Mapping mapping) {
			// Return servicer not matching of path
			return mapping.createServicerMapping("", mapping.getPath(),
					this.defaultServicer);
		}

		@Override
		public void mapAll(Mapping mapping, List<HttpServletServicer> servicers) {
			servicers.add(this.defaultServicer);
		}
	}

}