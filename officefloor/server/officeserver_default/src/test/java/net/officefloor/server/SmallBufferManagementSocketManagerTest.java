package net.officefloor.server;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Ensures all {@link StreamBuffer} instances are released.
 * 
 * @author Daniel Sagenschneider
 */
public class SmallBufferManagementSocketManagerTest extends BufferManagementSocketManagerTest {

	@Override
	protected int getBufferSize() {
		return 1;
	}

}