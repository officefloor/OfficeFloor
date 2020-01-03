package net.officefloor.jdbc;

import java.util.logging.Logger;

import javax.sql.DataSource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;

/**
 * {@link ManagedObjectSource} for a {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSource extends AbstractConnectionManagedObjectSource implements ManagedObject {

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/*
	 * ============== AbstractConnectionManagedObjectSource =================
	 */

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		this.loadFurtherMetaData(context);
	}

	@Override
	protected void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Capture the logger
		this.logger = mosContext.getLogger();

		// Load the type
		context.setObjectClass(DataSource.class);

		// Only load data source (if not loading type)
		if (mosContext.isLoadingType()) {
			return;
		}

		// Obtain the data source
		this.dataSource = this.newDataSource(mosContext);

		// Validate connectivity
		this.setConnectivity(() -> new ConnectionConnectivity(this.dataSource.getConnection()));
		this.loadValidateConnectivity(context);
	}

	@Override
	public void stop() {

		// Close the DataSource
		this.closeDataSource(this.dataSource, this.logger);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== ManagedObject ================================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.dataSource;
	}

}