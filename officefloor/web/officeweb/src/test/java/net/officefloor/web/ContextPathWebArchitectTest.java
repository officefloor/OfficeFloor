package net.officefloor.web;

import net.officefloor.web.build.WebArchitect;

/**
 * Tests the {@link WebArchitect} with a context path.
 * 
 * @author Daniel Sagenschneider
 */
public class ContextPathWebArchitectTest extends AbstractWebArchitectTest {

	@Override
	protected String getContextPath() {
		return "/context";
	}

}