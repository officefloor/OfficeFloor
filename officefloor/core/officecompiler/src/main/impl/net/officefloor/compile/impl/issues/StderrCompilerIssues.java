package net.officefloor.compile.impl.issues;

import net.officefloor.compile.issues.CompilerIssues;

/**
 * {@link CompilerIssues} to write issues to {@link System#err}.
 * 
 * @author Daniel Sagenschneider
 */
public class StderrCompilerIssues extends AbstractCompilerIssues {

	/*
	 * ================= AbstractCompilerIssues =================
	 */

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		CompileException.printIssue(issue, System.err);
	}

}