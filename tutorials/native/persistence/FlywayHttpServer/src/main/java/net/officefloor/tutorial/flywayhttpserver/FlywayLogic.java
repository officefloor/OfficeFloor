package net.officefloor.tutorial.flywayhttpserver;

import jakarta.persistence.EntityManager;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

public class FlywayLogic {

	// START SNIPPET: tutorial
	public void getMigration(@HttpQueryParameter("id") String migrationId, EntityManager entityManager,
			ObjectResponse<Migration> responder) {
		responder.send(entityManager.find(Migration.class, Long.parseLong(migrationId)));
	}
	// END SNIPPET: tutorial

}