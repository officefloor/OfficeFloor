package net.officefloor.jpa.eclipselink;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Eclipse Link {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class EclipseLinkJpaTest extends AbstractJpaTestCase {

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return EclipseLinkJpaManagedObjectSource.class;
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