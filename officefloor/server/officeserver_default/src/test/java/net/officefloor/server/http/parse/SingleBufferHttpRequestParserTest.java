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
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
		buffer.write(request);

		// Return parsing of the data
		parser.appendStreamBuffer(buffer);
		return parser.parse();
	}

}