package net.officefloor.web.build;

/**
 * Explorer of an execution tree for a {@link HttpInput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInputExplorer {

	/**
	 * Explores the execution tree for the {@link HttpInput}.
	 * 
	 * @param context {@link HttpInputExplorerContext}.
	 * @throws Exception If failure in exploring the {@link HttpInput}.
	 */
	void explore(HttpInputExplorerContext context) throws Exception;

}