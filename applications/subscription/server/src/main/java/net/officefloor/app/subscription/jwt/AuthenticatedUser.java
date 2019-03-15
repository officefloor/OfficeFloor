package net.officefloor.app.subscription.jwt;

import com.googlecode.objectify.Objectify;

import net.officefloor.app.subscription.store.User;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Authenticated user.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticatedUser {

	@Dependency
	private JwtClaims claims;

	@Dependency
	private Objectify objectify;

	private User user;

	/**
	 * Obtains the authenticated {@link User}.
	 * 
	 * @return Authenticated {@link User}.
	 */
	public User getAuthenticatedUser() {

		// Lazy create the authenticated user
		if (this.user != null) {

			// Obtain the user
			this.user = this.objectify.load().type(User.class).id(this.claims.getUserId()).now();
			if (this.user == null) {
				// Must have user (otherwise trigger login)
				throw new HttpException(HttpStatus.UNAUTHORIZED);
			}
		}

		// Return the authenticated user
		return user;
	}

}