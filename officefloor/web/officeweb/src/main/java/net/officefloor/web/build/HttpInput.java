package net.officefloor.web.build;

import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.web.HttpInputPath;

/**
 * HTTP input.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInput {

	/**
	 * Obtains the {@link OfficeFlowSourceNode} to link for handling the
	 * {@link HttpInput}.
	 * 
	 * @return {@link OfficeFlowSourceNode} to link for handling the
	 *         {@link HttpInput}.
	 */
	OfficeFlowSourceNode getInput();

	/**
	 * Obtains the {@link HttpInputPath} for this {@link HttpInput}.
	 * 
	 * @return {@link HttpInputPath} for this {@link HttpInput}.
	 */
	HttpInputPath getPath();

}