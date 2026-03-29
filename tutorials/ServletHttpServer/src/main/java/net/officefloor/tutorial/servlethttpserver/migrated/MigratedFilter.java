package net.officefloor.tutorial.servlethttpserver.migrated;

import java.io.IOException;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.tutorial.servlethttpserver.InjectedDependency;
import net.officefloor.tutorial.servlethttpserver.TutorialFilter;
import net.officefloor.web.HttpQueryParameter;

/**
 * Migrated {@link TutorialFilter} to OfficeFloor.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedFilter {

	@FlowInterface
	public static interface ChainNext {
		void doNext();
	}

	public void doFilter(ServerHttpConnection connection, @HttpQueryParameter("filter") String isFilter,
			ChainNext chain, InjectedDependency dependency) throws IOException {

		// Determine if filter
		if (Boolean.parseBoolean(isFilter)) {

			// Provide filter response
			connection.getResponse().getEntityWriter().write("FILTER " + dependency.getMessage());

		} else {

			// Carry on filter chain
			chain.doNext();
		}
	}
}
// END SNIPPET: tutorial