package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * {@link AbstractHttpRequestParserTestCase} that is worse case of writing a
 * byte per {@link StreamBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class SegmentedBufferHttpRequestParserTest extends AbstractHttpRequestParserTestCase {

	@Override
	protected boolean parse(HttpRequestParser parser, byte[] request) throws HttpException {

		// Create single buffer to progressively write the data
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(1));

		// Progressively write and parse a byte at a time
		boolean parseResult = false;
		for (int i = 0; i < request.length; i++) {

			// Add next segment of request
			StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
			buffer.write(request[i]);

			// Append same buffer with additional bytes
			parser.appendStreamBuffer(buffer);
			parseResult = parser.parse();
		}

		// Return last parse result
		return parseResult;
	}

}