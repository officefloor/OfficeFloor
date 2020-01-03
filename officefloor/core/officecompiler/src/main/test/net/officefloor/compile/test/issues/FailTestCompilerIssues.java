package net.officefloor.compile.test.issues;

import junit.framework.TestCase;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssues;

/**
 * {@link CompilerIssues} that invokes {@link TestCase#fail()} for issues
 * raised.
 * 
 * @author Daniel Sagenschneider
 */
public class FailTestCompilerIssues extends AbstractCompilerIssues {

	/*
	 * =================== AbstractCompilerIssues ===================
	 */

	@Override
	public CompileError addIssue(Node node, String issueDescription, Throwable cause) {

		// Enable test failures to bubble up
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		}

		// Handle non-test failure issue
		return super.addIssue(node, issueDescription, cause);
	}

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		TestCase.fail(new CompileException(issue).toString());
	}

}