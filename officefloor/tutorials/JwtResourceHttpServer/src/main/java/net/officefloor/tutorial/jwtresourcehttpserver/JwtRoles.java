package net.officefloor.tutorial.jwtresourcehttpserver;

import java.util.Arrays;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.jwt.role.JwtRoleCollector;

/**
 * Provides translation of {@link Claims} to roles.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtRoles {

	public void retrieveRoles(@Parameter JwtRoleCollector<Claims> collector) {
		collector.setRoles(Arrays.asList(collector.getClaims().getRoles()));
	}
}