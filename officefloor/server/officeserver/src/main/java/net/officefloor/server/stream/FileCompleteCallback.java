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