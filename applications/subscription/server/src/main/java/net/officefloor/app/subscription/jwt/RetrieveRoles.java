package net.officefloor.app.subscription.jwt;

import java.util.Arrays;

import net.officefloor.app.subscription.store.User;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.jwt.role.JwtRoleCollector;

/**
 * Retrieves the roles.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveRoles {

	public void retrieveRoles(@Parameter JwtRoleCollector<User> collector) {
		collector.setRoles(Arrays.asList(collector.getClaims().getRoles()));
	}
}