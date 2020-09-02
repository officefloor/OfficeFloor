package net.officefloor.jdbc.datasource;

import javax.sql.DataSource;

/**
 * Transforms the {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DataSourceTransformer {

	/**
	 * Transforms the {@link DataSource}.
	 * 
	 * @param context {@link DataSourceTransformerContext}.
	 * @return Transformed {@link DataSource}.
	 * @throws Exception If fails to transform the {@link DataSource}.
	 */
	DataSource transformDataSource(DataSourceTransformerContext context) throws Exception;

}