package net.officefloor.tutorial.servlethttpserver.migrated;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.tutorial.servlethttpserver.InjectedDependency;

/**
 * Migrated {@link MigratedServlet} to OfficeFloor.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedServlet {

	public void doGet(ServerHttpConnection connection, InjectedDependency dependency) throws IOException {
		connection.getResponse().getEntityWriter().write("SERVLET " + dependency.getMessage());
	}
}
// END SNIPPET: tutorial