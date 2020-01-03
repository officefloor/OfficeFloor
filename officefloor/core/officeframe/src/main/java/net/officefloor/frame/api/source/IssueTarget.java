package net.officefloor.frame.api.source;

/**
 * Receives the issue.
 * 
 * @author Daniel Sagenschneider
 */
public interface IssueTarget {

	/**
	 * Adds the issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	void addIssue(String issueDescription);

	/**
	 * Adds the issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	void addIssue(String issueDescription, Throwable cause);

}