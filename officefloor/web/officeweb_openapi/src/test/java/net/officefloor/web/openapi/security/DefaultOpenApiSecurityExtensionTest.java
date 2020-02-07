package net.officefloor.web.openapi.security;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.openapi.OpenApiSecurityExtension;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Tests the default {@link OpenApiSecurityExtension} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultOpenApiSecurityExtensionTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct {@link HttpSecuritySource} name for basic.
	 */
	public void testBasic() {
		assertEquals("Must match class", BasicHttpSecuritySource.class.getName(),
				BasicOpenApiSecurityExtension.BASIC_HTTP_SECURITY_SOURCE_CLASS_NAME);
	}

	/**
	 * Ensure correct {@link HttpSecuritySource} name for basic.
	 */
	public void testJwt() {
		assertEquals("Must match class", JwtHttpSecuritySource.class.getName(),
				JwtOpenApiSecurityExtension.JWT_HTTP_SECURITY_SOURCE_CLASS_NAME);
	}

}