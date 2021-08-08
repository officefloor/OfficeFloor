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

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Invokes on completion of writing the {@link FileChannel} content.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCompleteCallback {

	/**
	 * <p>
	 * Invoked on completion of writing the {@link FileChannel} content.
	 * <p>
	 * Note that may also be invoked if content was not written (rather
	 * cancelled).
	 * <p>
	 * Typical use is to close the {@link FileChannel} once complete.
	 * <p>
	 * <strong>WARNING:</strong> this is typically invoked on the
	 * {@link SocketChannel} {@link Thread} so should not invoke any long
	 * running operations.
	 * 
	 * @param file
	 *            {@link FileChannel} from the write.
	 * @param isWritten
	 *            <code>true</code> indicates whether written, while
	 *            <code>false</code> indicates cancelled.
	 * @throws IOException
	 *             If issue in interacting with {@link FileChannel}.
	 */
	void complete(FileChannel file, boolean isWritten) throws IOException;

}
