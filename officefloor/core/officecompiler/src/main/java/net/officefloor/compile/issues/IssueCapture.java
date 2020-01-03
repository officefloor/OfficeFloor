package net.officefloor.compile.issues;

import java.util.function.Supplier;

/**
 * Capture of {@link CompilerIssue}.
 *
 * @author Daniel Sagenschneider
 */
public interface IssueCapture<R> {

	/**
	 * Obtains the return value from the {@link Supplier}.
	 * 
	 * @return Return value.
	 */
	R getReturnValue();

	/**
	 * Obtains the {@link CompilerIssue} instances of the capture.
	 * 
	 * @return {@link CompilerIssue} instances of the capture. If no
	 *         {@link CompilerIssue} instances, then will return empty array.
	 */
	CompilerIssue[] getCompilerIssues();

}