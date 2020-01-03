package net.officefloor.compile.impl.structure;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.frame.api.source.IssueTarget;

/**
 * Adapts the {@link SourceIssues} to {@link IssueTarget}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceIssuesIssueTarget implements IssueTarget {

	/**
	 * {@link SourceIssues}.
	 */
	private final SourceIssues sourceIssues;

	/**
	 * Instantiate.
	 * 
	 * @param sourceIssues
	 *            {@link SourceIssues}.
	 */
	public SourceIssuesIssueTarget(SourceIssues sourceIssues) {
		this.sourceIssues = sourceIssues;
	}

	/*
	 * =================== IssueTarget =====================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.sourceIssues.addIssue(issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.sourceIssues.addIssue(issueDescription, cause);
	}

}