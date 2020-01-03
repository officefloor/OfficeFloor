package net.officefloor.web;

import net.officefloor.web.build.WebArchitect;

/**
 * Tests the {@link WebArchitect} without a context path.
 * 
 * @author Daniel Sagenschneider
 */
public class NoContextPathWebArchitectTest extends AbstractWebArchitectTest {

	@Override
	protected String getContextPath() {
		return null; // no context path
	}

}