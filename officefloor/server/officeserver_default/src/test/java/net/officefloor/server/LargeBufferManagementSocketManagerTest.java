package net.officefloor.server;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Ensures all {@link StreamBuffer} instances are released.
 * 
 * @author Daniel Sagenschneider
 */
public class LargeBufferManagementSocketManagerTest extends BufferManagementSocketManagerTest {

	@Override
	protected int getBufferSize() {
		return 128 * 1024;
	}

}