package net.officefloor.compile.impl.issues;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;

/**
 * Default {@link CompilerIssue}.
 *
 * @author Daniel Sagenschneider
 */
public class DefaultCompilerIssue implements CompilerIssue {

	/**
	 * {@link Node}.
	 */
	private final Node node;

	/**
	 * Issue description.
	 */
	private final String issueDescription;

	/**
	 * Optional cause.
	 */
	private final Throwable cause;

	/**
	 * Optional {@link CompilerIssue} causes.
	 */
	private final CompilerIssue[] causes;

	/**
	 * Instantiate.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Optional cause. May be <code>null</code>.
	 * @param causes
	 *            Optional {@link CompilerIssue} causes.
	 */
	public DefaultCompilerIssue(Node node, String issueDescription,
			Throwable cause, CompilerIssue[] causes) {
		this.node = node;
		this.issueDescription = issueDescription;
		this.cause = cause;
		this.causes = causes;
	}

	/**
	 * Obtains the {@link Node}.
	 * 
	 * @return {@link Node}.
	 */
	public Node getNode() {
		return this.node;
	}

	/**
	 * Obtains the issue description.
	 * 
	 * @return Issue description.
	 */
	public String getIssueDescription() {
		return this.issueDescription;
	}

	/**
	 * Obtains the optional cause.
	 * 
	 * @return Cause and may be <code>null</code>.
	 */
	public Throwable getCause() {
		return this.cause;
	}

	/**
	 * Obtains the optional {@link CompilerIssue} causes.
	 * 
	 * @return Optional {@link CompilerIssue} causes.
	 */
	public CompilerIssue[] getCauses() {
		return this.causes;
	}

}