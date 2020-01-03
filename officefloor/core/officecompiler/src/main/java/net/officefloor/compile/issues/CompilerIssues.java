package net.officefloor.compile.issues;

import java.util.function.Supplier;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Notified of issues in compilation of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompilerIssues {

	/**
	 * Captures {@link CompilerIssue} instances.
	 * 
	 * @param <R>
	 *            Return type of {@link Supplier} logic.
	 * @param supplier
	 *            {@link Supplier} of code to capture {@link CompilerIssue}
	 *            within.
	 * @return {@link IssueCapture}.
	 */
	<R> IssueCapture<R> captureIssues(Supplier<R> supplier);

	/**
	 * Adds an issue regarding a particular {@link Node}.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param causes
	 *            Possible {@link CompilerIssue} instances causing this issue.
	 * @return {@link CompileError} to be used in <code>throw</code>
	 *         statement when adding {@link CompilerIssue} to avoid further
	 *         compiling of the {@link Node}.
	 * @throws Error
	 *             If fail compile fast.
	 */
	CompileError addIssue(Node node, String issueDescription, CompilerIssue... causes);

	/**
	 * Adds an issue regarding a particular {@link Node}.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @return {@link CompileError} to be used in <code>throw</code>
	 *         statement when adding {@link CompilerIssue} to avoid further
	 *         compiling of the {@link Node}.
	 * @throws Error
	 *             If fail compile fast.
	 */
	CompileError addIssue(Node node, String issueDescription, Throwable cause);

}