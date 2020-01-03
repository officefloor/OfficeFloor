package net.officefloor.web.path;

/**
 * Tests with a context path.
 * 
 * @author Daniel Sagenschneider
 */
public class ContextPathTest extends AbstractPathTestCase {

	@Override
	protected String getContextPath() {
		return "/context";
	}

}