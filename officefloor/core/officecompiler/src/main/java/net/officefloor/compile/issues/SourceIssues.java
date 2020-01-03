package net.officefloor.compile.issues;

/**
 * Provides means to raise {@link CompilerIssue}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceIssues {

	/**
	 * <p>
	 * Allows the source to add an issue.
	 * <p>
	 * This is available to report invalid configuration.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @return {@link CompileError} to be used in <code>throw</code> statement
	 *         when adding {@link CompilerIssue} to avoid further compiling.
	 */
	CompileError addIssue(String issueDescription);

	/**
	 * <p>
	 * Allows the source to add an issue along with its cause.
	 * <p>
	 * This is available to report invalid configuration.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @return {@link CompileError} to be used in <code>throw</code> statement
	 *         when adding {@link CompilerIssue} to avoid further compiling.
	 */
	CompileError addIssue(String issueDescription, Throwable cause);

}