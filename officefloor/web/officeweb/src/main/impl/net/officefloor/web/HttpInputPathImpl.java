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

package net.officefloor.web;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpInputPathSegment.HttpInputPathSegmentEnum;
import net.officefloor.web.HttpPathFactoryImpl.ParameterSegment;
import net.officefloor.web.HttpPathFactoryImpl.Segment;
import net.officefloor.web.HttpPathFactoryImpl.StaticSegment;
import net.officefloor.web.build.HttpPathFactory;
import net.officefloor.web.value.retrieve.ValueRetriever;
import net.officefloor.web.value.retrieve.ValueRetrieverSource;

/**
 * {@link HttpInputPath} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpInputPathImpl implements HttpInputPath {

	/**
	 * Route path.
	 */
	private final String routePath;

	/**
	 * Head {@link HttpInputPathSegment} of linked list of
	 * {@link HttpInputPathSegment} instances.
	 */
	private final HttpInputPathSegment segmentHead;

	/**
	 * Number of path parameters for sorting routes.
	 */
	private final int parameterCount;

	/**
	 * Instantiate.
	 * 
	 * @param routePath      Route path.
	 * @param segmentHead    Head {@link HttpInputPathSegment} of linked list of
	 *                       {@link HttpInputPathSegment} instances.
	 * @param parameterCount Number of path parameters for sorting routes.
	 */
	public HttpInputPathImpl(String routePath, HttpInputPathSegment segmentHead, int parameterCount) {
		this.routePath = routePath;
		this.segmentHead = segmentHead;
		this.parameterCount = parameterCount;
	}

	/**
	 * ================ HttpInputPath =======================
	 */

	@Override
	public boolean isPathParameters() {
		return this.parameterCount > 0;
	}

	@Override
	public boolean isMatchPath(String path, int endingPathParameterTerminatingCharacter) {

		// Determine if match path
		HttpInputPathSegment segment = this.segmentHead;
		int pathIndex = 0;
		while (segment != null) {
			switch (segment.type) {
			case STATIC:
				// Ensure matches static path
				int staticLength = segment.value.length();
				if (!(path.regionMatches(pathIndex, segment.value, 0, staticLength))) {
					return false;
				}
				pathIndex += staticLength;
				break;

			case PARAMETER:
				// Obtain the terminating character for parameter
				int terminator;
				if (segment.next != null) {
					// Ensure is followed by static path
					if (segment.next.type != HttpInputPathSegmentEnum.STATIC) {
						throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
								"Path parameter must only be followed by static path content, but was "
										+ segment.next.type.name());
					}

					// First static character terminates parameter
					terminator = segment.next.value.charAt(0);

				} else {
					// Parameter is last, so use ending path terminator
					terminator = endingPathParameterTerminatingCharacter;
				}

				// Loop until reach terminator (or end of path)
				FOUND_TERMINATOR: while (pathIndex < path.length()) {
					if (path.charAt(pathIndex) == terminator) {
						break FOUND_TERMINATOR;
					}
					pathIndex++;
				}
				break;

			default:
				throw new IllegalStateException("Route should not have segment of type " + segment.type);
			}

			// Next segment
			segment = segment.next;
		}

		// Ensure match full path
		if (pathIndex != path.length()) {
			return false; // did not match full path
		}

		// As here, matches
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> HttpPathFactory<T> createHttpPathFactory(Class<T> valuesType) throws HttpException {

		// Ensure have type
		if (valuesType == null) {
			valuesType = (Class<T>) Void.TYPE;
		}

		// Create the value retriever
		ValueRetrieverSource source = new ValueRetrieverSource(true);
		ValueRetriever<T> valueRetriever = source.sourceValueRetriever(valuesType);

		// Create the segments
		List<Segment<T>> segments = new ArrayList<>();
		HttpInputPathSegment segment = this.segmentHead;
		while (segment != null) {
			switch (segment.type) {
			case STATIC:
				// Add the static part of path
				segments.add(new StaticSegment<T>(segment.value));
				break;
			case PARAMETER:
				// Ensure parameter value is available
				if (valueRetriever.getValueType(segment.value) == null) {
					throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "For path '" + this.routePath
							+ "', no property '" + segment.value + "' on object " + valuesType.getName());
				}

				// Add the parameter
				segments.add(new ParameterSegment<T>(segment.value, valueRetriever));
				break;
			default:
				throw new IllegalStateException("Route should not have segment of type " + segment.type);
			}
			segment = segment.next;
		}

		// Create the HTTP path factory
		return new HttpPathFactoryImpl<T>(valuesType, segments.toArray(new Segment[segments.size()]));
	}

}
