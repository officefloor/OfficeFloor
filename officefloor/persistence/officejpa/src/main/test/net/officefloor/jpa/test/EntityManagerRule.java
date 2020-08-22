/*-
 * #%L
 * JPA Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jpa.test;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;

/**
 * {@link EntityManager} {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class EntityManagerRule implements TestRule {

	/**
	 * Path to the {@link EntityManager} properties file.
	 */
	private final String entityManagerPropertiesFilePath;

	/**
	 * {@link PersistenceFactory}.
	 */
	private final PersistenceFactory persistenceFactory;

	/**
	 * {@link DatabaseTestUtil}.
	 */
	private final DatabaseTestUtil dataSourceRule;

	/**
	 * {@link EntityManager}.
	 */
	private EntityManager entityManager = null;

	/**
	 * Instantiate.
	 * 
	 * @param persistenceFactory
	 *            {@link PersistenceFactory}.
	 * @param entityManagerPropertiesFilePath
	 *            Path to the {@link EntityManager} properties file.
	 * @param dataSourceRule
	 *            {@link DatabaseTestUtil}. The {@link DatabaseTestUtil} should be
	 *            registered as {@link ClassRule}.
	 */
	public EntityManagerRule(String entityManagerPropertiesFilePath, PersistenceFactory persistenceFactory,
			DatabaseTestUtil dataSourceRule) {
		this.entityManagerPropertiesFilePath = entityManagerPropertiesFilePath;
		this.persistenceFactory = persistenceFactory;
		this.dataSourceRule = dataSourceRule;
	}

	/**
	 * Obtains the {@link EntityManager}.
	 * 
	 * @return {@link EntityManager}.
	 */
	public EntityManager getEntityManager() {
		if (this.entityManager == null) {
			throw new IllegalStateException(EntityManager.class.getSimpleName() + " only available during test");
		}
		return this.entityManager;
	}

	/*
	 * ================= TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Ensure properties name starts with / (so absolute on class path)
				String propertiesFileName = EntityManagerRule.this.entityManagerPropertiesFilePath;
				if (!propertiesFileName.startsWith("/")) {
					propertiesFileName = "/" + propertiesFileName;
				}

				// Obtain the properties
				InputStream propertiesFile = DefaultDataSourceFactory.class.getResourceAsStream(propertiesFileName);
				if (propertiesFile == null) {
					throw new FileNotFoundException("Can not find file " + propertiesFileName + " on the class path");
				}
				Properties properties = new Properties();
				properties.load(propertiesFile);

				// Obtain the persistent unit name
				String persistenceUnitName = properties.getProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT);
				assertNotNull("Persistence unit name not configured in properties file "
						+ EntityManagerRule.this.entityManagerPropertiesFilePath, persistenceUnitName);

				// Obtain the data source
				DataSource dataSource = EntityManagerRule.this.dataSourceRule.getDataSource();

				// Create the entity manager
				EntityManagerFactory emFactory = EntityManagerRule.this.persistenceFactory
						.createEntityManagerFactory(persistenceUnitName, dataSource, properties);
				EntityManager entityManager = emFactory.createEntityManager();

				try {

					// Allow access to entity manager
					EntityManagerRule.this.entityManager = entityManager;

					// Run entity manager within managed transaction
					entityManager.getTransaction().begin();

					// Undertake the test
					base.evaluate();

					// Commit the transaction (if within transaction)
					if (entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().commit();
					}

				} catch (Throwable ex) {
					// Roll back the transaction
					entityManager.getTransaction().rollback();

					// Propagate the exception
					throw ex;

				} finally {
					// Close the entity manager
					entityManager.close();

					// Clear the entity manager
					EntityManagerRule.this.entityManager = null;
				}
			}
		};
	}

}
