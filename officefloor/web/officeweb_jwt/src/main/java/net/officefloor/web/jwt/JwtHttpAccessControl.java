package net.officefloor.web.jwt;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Extra JWT functions to {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtHttpAccessControl<C> extends HttpAccessControl {

	/**
	 * <p>
	 * Obtains the JWT claims.
	 * <p>
	 * While this is available, it does reduce the re-usability of the
	 * {@link HttpAccessControl} (and resulting infrastructure built on it).
	 * Ideally, this should not be used with {@link #inRole(String)} being
	 * preferred.
	 * <p>
	 * However, if direct access to the claim is required, it should just be
	 * depended on as a custom access control object. For example, a
	 * {@link ManagedObjectSource} can be created to depend on the claims object and
	 * data store. This {@link ManagedObjectSource} can retrieve the user entry for
	 * the JWT claims from the data store and make available for dependency
	 * injection. This provides a re-usable application centric
	 * {@link ManagedObjectSource} that does not depend on (possibly changing)
	 * OfficeFloor enforced "standard" {@link HttpSecurity} interfaces.
	 * 
	 * @return JWT claims.
	 */
	C getClaims();

}