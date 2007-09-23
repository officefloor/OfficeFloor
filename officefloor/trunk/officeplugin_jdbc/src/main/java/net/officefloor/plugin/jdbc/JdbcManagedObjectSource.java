/*
 * Created on Jan 25, 2006
 */
package net.officefloor.plugin.jdbc;

import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for JDBC.
 * 
 * @author Daniel
 */
public class JdbcManagedObjectSource implements ManagedObjectSource,
		ManagedObjectSourceMetaData {

	/**
	 * Property name to obtain the class of the {@link DataSourceFactory}.
	 */
	public static final String DATA_SOURCE_FACTORY_CLASS_PROPERTY = "net.officefloor.plugin.jdbc.datasourcefactory";

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private ConnectionPoolDataSource poolDataSource;

	/**
	 * Default constructor as required.
	 */
	public JdbcManagedObjectSource() {
	}

	/**
	 * Obtains the {@link DataSourceFactory}from the input properties.
	 * 
	 * @param properties
	 *            Properties to create and configure the
	 *            {@link DataSourceFactory}.
	 * @return Configured {@link DataSourceFactory}.
	 * @throws Exception
	 *             Should there be a failure creating or configuring the
	 *             {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory(Properties properties)
			throws Exception {

		// Obtain the name of the data source factory
		String className = properties
				.getProperty(DATA_SOURCE_FACTORY_CLASS_PROPERTY);

		// Create an instance of the data source factory
		DataSourceFactory dataSourceFactory = (DataSourceFactory) Class
				.forName(className).newInstance();

		// Initiate the data source factory
		dataSourceFactory.init(properties);

		// Return the configured data source factory
		return dataSourceFactory;
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	public ManagedObjectSourceSpecification getSpecification() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {
		// Obtain the Data Source Factory
		DataSourceFactory sourceFactory = this.getDataSourceFactory(context
				.getProperties());

		// Create the data source
		this.poolDataSource = sourceFactory.createConnectionPoolDataSource();

		// Create the recycle task
		new RecycleJdbcTask().registerAsRecycleTask(context, "jdbc.recycle");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	public ManagedObjectSourceMetaData getMetaData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	public void start(ManagedObjectExecuteContext context) throws Exception {
		// No starting
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		try {
			// Obtain the pooled connection
			PooledConnection pooledConnection = this.poolDataSource
					.getPooledConnection();

			// Return the jdbc mo
			user.setManagedObject(new JdbcManagedObject(pooledConnection));

		} catch (Throwable ex) {
			user.setFailure(ex);
		}
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	public Class getManagedObjectClass() {
		return JdbcManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class getObjectClass() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class getDependencyKeys() {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(Enum key) {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	public Class getHandlerKeys() {
		// No handlers
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class getHandlerType(Enum key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData[] getExtensionInterfacesMetaData() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}
}