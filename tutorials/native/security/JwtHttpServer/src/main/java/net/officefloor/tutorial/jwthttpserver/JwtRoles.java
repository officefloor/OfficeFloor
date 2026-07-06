package net.officefloor.tutorial.jwthttpserver;

import java.util.Arrays;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.jwt.role.JwtRoleCollector;

public class JwtRoles {

	public void retrieveRoles(@Parameter JwtRoleCollector<Claims> collector) {
		collector.setRoles(Arrays.asList(collector.getClaims().getRoles()));
	}
}