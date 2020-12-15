/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * {@link AbstractHttpRequestParserTestCase} that uses a single
 * {@link ByteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class SingleBufferHttpRequestParserTest extends AbstractHttpRequestParserTestCase {

	@Override
	protected boolean parse(HttpRequestParser parser, byte[] request) throws HttpException {

		// Create single buffer with data
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocateDirect(request.length));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		buffer.write(request);

		// Return parsing of the data
		parser.appendStreamBuffer(buffer);
		return parser.parse();
	}

}
