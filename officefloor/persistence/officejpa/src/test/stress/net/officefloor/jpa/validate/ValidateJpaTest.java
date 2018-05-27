/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.jpa.validate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.datanucleus.enhancer.DataNucleusEnhancer;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Tests DataNucleus JPA implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateJpaTest extends AbstractJpaTestCase {

	static {
		// Enhance the classes
		DataNucleusEnhancer enhancer = new DataNucleusEnhancer("JPA", null);
		enhancer.setVerbose(true);
		enhancer.addPersistenceUnit("test");
		enhancer.enhance();
	}

	@Override
	protected void loadJpaProperties(PropertyConfigurable mos) {

		// Load the properties
		mos.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
		mos.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_FACTORY,
				DataNucleusPersistenceFactory.class.getName());
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

	/**
	 * DataNucleus {@link PersistenceFactory}.
	 */
	public static class DataNucleusPersistenceFactory implements PersistenceFactory {

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
				Properties properties) throws Exception {
			Map configuration = new HashMap<>(properties);
			configuration.put("datanucleus.ConnectionFactory", dataSource);
			return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
		}
	}

}