package net.officefloor.compile.spi.office;

/**
 * Input into the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionInput extends OfficeFlowSinkNode {

	/**
	 * Obtains the {@link OfficeSection} containing this
	 * {@link OfficeSectionInput}.
	 * 
	 * @return {@link OfficeSection} containing this {@link OfficeSectionInput}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Obtains the name of this {@link OfficeSectionInput}.
	 * 
	 * @return Name of this {@link OfficeSectionInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Adds an {@link ExecutionExplorer} for the execution tree from this
	 * {@link OfficeSectionInput}.
	 * 
	 * @param executionExplorer
	 *            {@link ExecutionExplorer}.
	 */
	void addExecutionExplorer(ExecutionExplorer executionExplorer);

}