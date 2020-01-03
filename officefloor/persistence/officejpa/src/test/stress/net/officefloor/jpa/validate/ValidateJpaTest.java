package net.officefloor.jpa.validate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Tests JPA implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateJpaTest extends AbstractJpaTestCase {

	@Override
	protected void loadJpaProperties(PropertyConfigurable mos) {

		// Load the properties
		mos.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
		mos.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_FACTORY,
				HibernatePersistenceFactory.class.getName());
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

	/**
	 * Hibernate {@link PersistenceFactory}.
	 */
	public static class HibernatePersistenceFactory implements PersistenceFactory {

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
				Properties properties) throws Exception {
			Map configuration = new HashMap<>(properties);
			if (dataSource != null) {
				configuration.put("hibernate.connection.datasource", dataSource);
			}
			return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
		}
	}

}