package net.officefloor.compile.issues;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link CompilerIssue} {@link Error} to enable propagating to
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileError extends Error {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param issueDescription Description of {@link CompilerIssue}.
	 */
	public CompileError(String issueDescription) {
		super(issueDescription);
	}

}