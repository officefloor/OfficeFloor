package net.officefloor.tutorial.jwtresourcehttpserver;

import java.util.Arrays;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.jwt.role.JwtRoleCollector;

// START SNIPPET: tutorial
public class JwtRoles {

	public void retrieveRoles(@Parameter JwtRoleCollector<Claims> collector) {
		collector.setRoles(Arrays.asList(collector.getClaims().getRoles()));
	}
}
// END SNIPPET: tutorial