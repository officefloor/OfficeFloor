package net.officefloor.jpa.datanucleus;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * DataNucleus {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataNucleusJpaTest extends AbstractJpaTestCase {

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return DataNucleusJpaManagedObjectSource.class;
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