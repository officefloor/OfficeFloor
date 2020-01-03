package net.officefloor.web.route;

/**
 * {@link AbstractWebRouterTest} with no context path.
 * 
 * @author Daniel Sagenschneider
 */
public class ContextWebRouterTest extends AbstractWebRouterTest {

	@Override
	protected String getContextPath() {
		return "/context";
	}

}