package net.officefloor.server.ssl;

import net.officefloor.server.ThreadLocalPoolSocketManagerTest;

/**
 * SSL {@link ThreadLocalPoolSocketManagerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslThreadLocalPoolSocketManagerTest extends ThreadLocalPoolSocketManagerTest {

	public SslThreadLocalPoolSocketManagerTest() {
		this.isSecure = true;
	}

}