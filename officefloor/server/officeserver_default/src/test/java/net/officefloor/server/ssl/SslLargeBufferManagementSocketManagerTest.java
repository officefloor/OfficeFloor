package net.officefloor.server.ssl;

import net.officefloor.server.LargeBufferManagementSocketManagerTest;

/**
 * SSL {@link LargeBufferManagementSocketManagerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslLargeBufferManagementSocketManagerTest extends LargeBufferManagementSocketManagerTest {

	public SslLargeBufferManagementSocketManagerTest() {
		this.isSecure = true;
	}

}