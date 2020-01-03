package net.officefloor.jpa.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;

/**
 * Hibernate {@link JpaManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateJpaManagedObjectSource extends JpaManagedObjectSource implements PersistenceFactory {

	/*
	 * =============== JpaManagedObjectSource =================
	 */

	@Override
	protected PersistenceFactory getPersistenceFactory(MetaDataContext<Indexed, None> context) throws Exception {
		return this;
	}

	/*
	 * ================= PersistenceFactory ===================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
			Properties properties) throws Exception {
		Map configuration = new HashMap<>(properties);
		if (dataSource != null) {
			configuration.put("hibernate.connection.datasource", dataSource);
		}
		return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
	}

}