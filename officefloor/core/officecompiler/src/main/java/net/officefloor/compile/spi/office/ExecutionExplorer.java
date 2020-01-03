package net.officefloor.compile.spi.office;

/**
 * Explorer of an execution tree.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionExplorer {

	/**
	 * Explores the execution tree for the {@link OfficeSectionInput}.
	 * 
	 * @param context
	 *            {@link ExecutionExplorerContext}.
	 * @throws Exception
	 *             If failure in exploring the {@link OfficeSectionInput}.
	 */
	void explore(ExecutionExplorerContext context) throws Exception;

}