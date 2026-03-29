package net.officefloor.jpa.eclipselink;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;

/**
 * Eclipse Link {@link JpaManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class EclipseLinkJpaManagedObjectSource extends JpaManagedObjectSource implements PersistenceFactory {

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
			configuration.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);
		}
		return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
	}

}