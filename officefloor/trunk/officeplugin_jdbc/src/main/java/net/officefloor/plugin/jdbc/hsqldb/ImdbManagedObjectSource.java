/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.jdbc.hsqldb;

import java.util.Properties;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.jdbc.DataSourceFactory;
import net.officefloor.plugin.jdbc.JdbcManagedObjectSource;

/**
 * <p>
 * {@link ManagedObjectSource} for an IMDB.
 * <p>
 * Convenience {@link ManagedObjectSource} for the
 * {@link HsqldbImdbDataSourceFactory}.
 * 
 * @author Daniel
 */
public class ImdbManagedObjectSource extends JdbcManagedObjectSource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.JdbcManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Ensure name of database specified
		context.addProperty(HsqldbImdbDataSourceFactory.HSQLDB_DATABASE_NAME,
				"Database name");

		// Ensure an initialise script is provided
		context.addProperty(
				JdbcManagedObjectSource.DATA_SOURCE_INITIALISE_SCRIPT,
				"Initialise script");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.JdbcManagedObjectSource#getDataSourceFactory(java.util.Properties)
	 */
	@Override
	protected DataSourceFactory getDataSourceFactory(Properties properties)
			throws Exception {
		// Always IMDB
		return new HsqldbImdbDataSourceFactory();
	}

}
