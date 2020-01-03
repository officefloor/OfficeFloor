package net.officefloor.web.path;

/**
 * Tests with a deep context path. In other words, multiple directory deep
 * context path.
 * 
 * @author Daniel Sagenschneider
 */
public class DeepContextPathTest extends AbstractPathTestCase {

	@Override
	protected String getContextPath() {
		return "/context/path";
	}

}