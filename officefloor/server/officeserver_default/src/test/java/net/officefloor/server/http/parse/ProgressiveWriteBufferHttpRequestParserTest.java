/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * {@link AbstractHttpRequestParserTestCase} that progressively writes the
 * content to the parser.
 * 
 * @author Daniel Sagenschneider
 */
public class ProgressiveWriteBufferHttpRequestParserTest extends AbstractHttpRequestParserTestCase {

	@Override
	protected boolean parse(HttpRequestParser parser, byte[] request) throws HttpException {

		// Create single buffer to progressively write the data
		try (MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(request.length))) {
			StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();

			// Progressively write and parse a byte at a time
			boolean parseResult = false;
			for (int i = 0; i < request.length; i++) {

				// Progressively write the next byte
				buffer.write(request[i]);

				// Append same buffer with additional bytes
				parser.appendStreamBuffer(buffer);
				parseResult = parser.parse();
			}

			// Return last parse result
			return parseResult;
		}
	}

}
