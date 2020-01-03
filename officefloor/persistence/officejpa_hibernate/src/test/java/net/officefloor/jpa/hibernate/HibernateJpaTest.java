package net.officefloor.jpa.hibernate;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateJpaTest extends AbstractJpaTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Ignore hibernate logging
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
	}

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return HibernateJpaManagedObjectSource.class;
	}

	@Override
	protected void loadJpaProperties(PropertyConfigurable jpa) {
		jpa.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

}