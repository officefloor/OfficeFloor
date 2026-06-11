package net.officefloor.tutorial.flywayhttpserver;

import org.flywaydb.core.Flyway;

import jakarta.persistence.EntityManager;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic to provide data from {@link Flyway} migration.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayLogic {

	// START SNIPPET: tutorial
	public void getMigration(@HttpQueryParameter("id") String migrationId, EntityManager entityManager,
			ObjectResponse<Migration> responder) {
		responder.send(entityManager.find(Migration.class, Long.parseLong(migrationId)));
	}
	// END SNIPPET: tutorial

}