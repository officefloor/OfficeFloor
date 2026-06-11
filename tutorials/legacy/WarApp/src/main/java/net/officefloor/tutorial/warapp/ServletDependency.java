package net.officefloor.tutorial.warapp;

/**
 * Dependency for the {@link InjectServlet}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ServletDependency {

	public String getMessage() {
		return "INJECTED";
	}
}
// END SNIPPET: tutorial