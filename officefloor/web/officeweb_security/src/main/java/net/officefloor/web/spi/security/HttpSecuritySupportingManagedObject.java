package net.officefloor.web.spi.security;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * <p>
 * {@link HttpSecuritySource} configured {@link ManagedObject} to provide
 * supporting dependencies to the {@link HttpAuthentication} and
 * {@link HttpAccessControl} for using the {@link HttpSecuritySource}.
 * <p>
 * An example is a JWT authority {@link ManagedObject} that generates JWTs for
 * sending to the client. These JWTs are then used by the {@link HttpSecurity}
 * to secure the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObject extends PropertyConfigurable {
}