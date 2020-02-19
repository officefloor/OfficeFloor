package net.officefloor.compile.spi.office;

/**
 * Explorer of execution tree once other exploration is complete.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompletionExplorer {

	/**
	 * Undertakes exploration completion.
	 * 
	 * @throws Exception If failure in completing exploration.
	 */
	void complete() throws Exception;

}