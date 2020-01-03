package net.officefloor.web.resource.spi;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link FileCacheService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCacheServiceContext {

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

	/**
	 * Obtains the {@link SourceIssues}.
	 * 
	 * @return {@link SourceIssues}.
	 */
	SourceIssues getSourceIssues();

}