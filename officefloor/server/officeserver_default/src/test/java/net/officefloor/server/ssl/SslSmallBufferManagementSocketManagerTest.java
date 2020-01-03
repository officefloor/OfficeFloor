package net.officefloor.server.ssl;

import net.officefloor.server.SmallBufferManagementSocketManagerTest;

/**
 * SSL {@link SmallBufferManagementSocketManagerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslSmallBufferManagementSocketManagerTest extends SmallBufferManagementSocketManagerTest {

	public SslSmallBufferManagementSocketManagerTest() {
		this.isSecure = true;
	}

}