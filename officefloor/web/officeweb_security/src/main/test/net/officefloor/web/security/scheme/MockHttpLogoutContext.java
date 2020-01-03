package net.officefloor.web.security.scheme;

import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.LogoutContext;

/**
 * Mock {@link LogoutContext} for testing {@link HttpSecuritySource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpLogoutContext<O extends Enum<O>, F extends Enum<F>>
		extends AbstractMockHttpSecurityActionContext<O, F> implements LogoutContext<O, F> {
}