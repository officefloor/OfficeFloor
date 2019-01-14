package net.officefloor.web.jwt;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.spi.decode.JwtDecodeCollector;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;

/**
 * Tests the {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("unchecked")
public class LoadJwtHttpSecurityTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(JwtHttpSecuritySource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(JwtHttpAccessControl.class);
		type.setInput(true);
		type.addFlow(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS, JwtDecodeCollector.class);

		// Validate the type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, JwtHttpSecuritySource.class);
	}

}