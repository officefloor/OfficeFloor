/*-
 * #%L
 * DataNucleus JPA Persistence
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

package net.officefloor.jpa.datanucleus;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;

/**
 * DataNucleus {@link JpaManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataNucleusJpaManagedObjectSource extends JpaManagedObjectSource implements PersistenceFactory {

	/*
	 * =============== JpaManagedObjectSource =================
	 */

	@Override
	protected PersistenceFactory getPersistenceFactory(SourceContext context) throws Exception {
		return this;
	}

	@Override
	protected boolean isRunWithinTransaction() {
		return false;
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
			configuration.put("datanucleus.ConnectionFactory", dataSource);
		}
		return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
	}

}
