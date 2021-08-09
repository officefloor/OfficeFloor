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

package net.officefloor.server;

import java.nio.ByteBuffer;

import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Ensures all {@link StreamBuffer} instances are released.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferManagementSocketManagerTest extends AbstractSocketManagerTestCase {

	@Override
	protected int getBufferSize() {
		return 1024;
	}

	@Override
	protected StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize) {
		return new MockStreamBufferPool(() -> ByteBuffer.allocateDirect(bufferSize));
	}

	@Override
	protected void handleCompletion(StreamBufferPool<ByteBuffer> bufferPool) {
		((MockStreamBufferPool) bufferPool).assertAllBuffersReturned();
	}

}
