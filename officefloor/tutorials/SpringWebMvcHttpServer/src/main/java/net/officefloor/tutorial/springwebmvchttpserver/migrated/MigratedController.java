package net.officefloor.tutorial.springwebmvchttpserver.migrated;

import lombok.Value;
import net.officefloor.web.HttpQueryParameter;

/**
 * Migrated Spring Web MVC Controller.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedController {

	@Value
	public static class Model {
		private String name;
	}

	public Model getTemplate(@HttpQueryParameter("name") String name) {
		return new Model(name);
	}
}
// END SNIPPET: tutorial