package net.officefloor.jdbc.datasource;

import javax.sql.DataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link DataSourceTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DataSourceTransformerContext {

	/**
	 * Obtains the {@link DataSource} to transform.
	 * 
	 * @return {@link DataSource} to transform.
	 */
	DataSource getDataSource();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}