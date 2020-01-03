package net.officefloor.web.path;

/**
 * Tests no context path.
 * 
 * @author Daniel Sagenschneider
 */
public class NoContextPathTest extends AbstractPathTestCase {

	@Override
	protected String getContextPath() {
		return null;
	}

}