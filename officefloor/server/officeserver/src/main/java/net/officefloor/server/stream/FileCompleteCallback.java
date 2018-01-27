/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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