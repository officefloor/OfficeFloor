package net.officefloor.server.ssl;

import javax.net.ssl.SSLSocket;

import net.officefloor.server.BufferManagementSocketManagerTest;
import net.officefloor.server.SocketManager;

/**
 * Tests {@link SSLSocket} communication with {@link SocketManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslSocketManagerTest extends BufferManagementSocketManagerTest {

	public SslSocketManagerTest() {
		this.isSecure = true;
	}

}