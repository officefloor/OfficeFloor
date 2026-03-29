/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.stream;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Fix for compatibility issue between JDK8 and JDK9.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferJvmFix {

	/**
	 * Handles difference in flip.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Result of flip.
	 */
	public static Buffer flip(Buffer buffer) {
		return buffer.flip();
	}

	/**
	 * Handle difference in clear.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Result of clear.
	 */
	public static Buffer clear(Buffer buffer) {
		return buffer.clear();
	}

	/**
	 * Handle difference in position.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Position.
	 */
	public static int position(Buffer buffer) {
		return buffer.position();
	}

	/**
	 * Handles difference in position.
	 * 
	 * @param buffer   {@link ByteBuffer}.
	 * @param position Position.
	 * @return Result of position.
	 */
	public static Buffer position(Buffer buffer, int position) {
		return buffer.position(position);
	}

	/**
	 * Handle difference in limit.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Limit.
	 */
	public static int limit(Buffer buffer) {
		return buffer.limit();
	}

	/**
	 * Handle difference in limit.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @param limit  Limit.
	 * @return Result of limit.
	 */
	public static Buffer limit(Buffer buffer, int limit) {
		return buffer.limit(limit);
	}

	/**
	 * All access via static methods.
	 */
	private BufferJvmFix() {
	}
}
