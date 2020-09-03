package net.officefloor.tutorial.flywayhttpserver;

import javax.persistence.EntityManager;

import org.flywaydb.core.Flyway;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic to provide data from {@link Flyway} migration.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayLogic {

	public void getMigration(@HttpQueryParameter("id") String migrationId, EntityManager entityManager,
			ObjectResponse<Migration> responder) {
		responder.send(entityManager.find(Migration.class, Long.parseLong(migrationId)));
	}

}