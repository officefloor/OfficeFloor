/*-
 * #%L
 * HTTP Server
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
	 * All access via static methods.
	 */
	private BufferJvmFix() {
	}
}
