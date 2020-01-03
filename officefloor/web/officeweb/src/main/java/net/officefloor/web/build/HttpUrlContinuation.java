package net.officefloor.web.build;

import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;

/**
 * HTTP URL continuation.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpUrlContinuation extends HttpInput {

	/**
	 * Obtains the {@link OfficeFlowSinkNode} to link to this
	 * {@link HttpUrlContinuation}.
	 * 
	 * @param parameterTypeName
	 *            Name of the {@link Class} providing the possible parameters
	 *            for the {@link HttpUrlContinuation} path. May be
	 *            <code>null</code> if {@link HttpUrlContinuation} path contains
	 *            no parameters.
	 * @return {@link OfficeFlowSinkNode}.
	 * @throws CompileError
	 *             If fails to create {@link OfficeFlowSinkNode} with the
	 *             parameter type.
	 */
	OfficeFlowSinkNode getRedirect(String parameterTypeName) throws CompileError;

}