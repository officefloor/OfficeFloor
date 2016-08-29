/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.issues;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the abstract functionality of the {@link AbstractCompilerIssues}.
 *
 * @author Daniel Sagenschneider
 */
public class AbstractCompilerIssuesTest extends OfficeFrameTestCase {

	/**
	 * {@link AbstractCompilerIssues} to test.
	 */
	private final MockAbstractCompilerIssues issues = new MockAbstractCompilerIssues();

	/**
	 * Mock {@link Node}.
	 */
	private final Node node = this.createMock(Node.class);

	/**
	 * Ensure handle issue.
	 */
	public void testHandleSimpleIssue() {
		this.issues.addIssue(this.node, "TEST ISSUE");
		assertCompilerIssues(new Issue("TEST ISSUE"));
	}

	/**
	 * Ensure handle failure.
	 */
	public void testHandleSimpleFailure() {
		Throwable cause = new NullPointerException("TEST CAUSE");
		this.issues.addIssue(this.node, "TEST FAILURE", cause);
		assertCompilerIssues(new Issue("TEST FAILURE", cause));
	}

	/**
	 * Ensure capture simple issue.
	 */
	public void testCapturedSimpleIssue() {
		CompilerIssue[] issues = this.issues.captureIssues(() -> {
			this.issues.addIssue(this.node, "TEST ISSUE");
		});
		assertCompilerIssues(issues, new Issue("TEST ISSUE"));
	}

	/**
	 * Ensure capture simple failure.
	 */
	public void testCaptureSimpleFailure() {
		Throwable cause = new IOException("TEST CAUSE");
		CompilerIssue[] issues = this.issues.captureIssues(() -> {
			this.issues.addIssue(this.node, "TEST FAILURE", cause);
		});
		assertCompilerIssues(issues, new Issue("TEST FAILURE", cause));
	}

	/**
	 * Ensure capture multiple issues.
	 */
	public void testCaptureIssueAndFailure() {
		Throwable cause = new SQLException("TEST CAUSE");
		CompilerIssue[] issues = this.issues.captureIssues(() -> {
			this.issues.addIssue(this.node, "TEST ISSUE");
			this.issues.addIssue(this.node, "TEST FAILURE", cause);
		});
		assertCompilerIssues(issues, new Issue("TEST ISSUE"), new Issue(
				"TEST FAILURE", cause));
	}

	/**
	 * Ensure can capture issues at multiple levels.
	 */
	public void testCaptureIssuesAtMultipleLevels() {
		CompilerIssue[] issues = this.issues.captureIssues(() -> {
			this.issues.addIssue(
					this.node,
					"TOP",
					this.issues.captureIssues(() -> {
						this.issues.addIssue(this.node, "SECOND",
								this.issues.captureIssues(() -> {
									this.issues.addIssue(this.node, "THIRD");
								}));
					}));
		});
		assertCompilerIssues(issues, new Issue("TOP", new Issue("SECOND",
				new Issue("THIRD"))));
	}

	/**
	 * Asserts the reported {@link CompilerIssue} instances are as expected.
	 * 
	 * @param issues
	 *            Expected issues.
	 */
	private void assertCompilerIssues(Issue... issues) {
		assertCompilerIssues(
				this.issues.reportedIssues.toArray(new CompilerIssue[0]),
				issues);
	}

	/**
	 * Asserts the {@link CompilerIssue} instances.
	 * 
	 * @param compilerIssues
	 *            {@link CompilerIssue} to validate.
	 * @param issues
	 *            Expected issues.
	 */
	private void assertCompilerIssues(CompilerIssue[] compilerIssues,
			Issue... issues) {
		assertEquals(compilerIssues.length, issues.length);
		for (int i = 0; i < issues.length; i++) {
			CompilerIssue compilerIssue = compilerIssues[i];
			Issue issue = issues[i];
			assertCompilerIssue(compilerIssues[i], issue.issueDescription,
					issue.failure);
			DefaultCompilerIssue defaultIssue = (DefaultCompilerIssue) compilerIssue;
			assertCompilerIssues(defaultIssue.getCauses(), issue.causes);
		}
	}

	/**
	 * Asserts the {@link CompilerIssue}.
	 * 
	 * @param issue
	 *            {@link CompilerIssue}.
	 * @param description
	 *            Expected issue description.
	 * @param cause
	 *            Expected cause.
	 */
	private void assertCompilerIssue(CompilerIssue issue, String description,
			Throwable cause) {
		assertEquals(DefaultCompilerIssue.class, issue.getClass());
		DefaultCompilerIssue defaultIssue = (DefaultCompilerIssue) issue;
		assertSame(this.node, defaultIssue.getNode());
		assertEquals(description, defaultIssue.getIssueDescription());
		assertSame(cause, defaultIssue.getCause());
	}

	/**
	 * Struct to enable detailing the expected {@link CompilerIssue} structure.
	 */
	private static class Issue {

		/**
		 * Issue description.
		 */
		private final String issueDescription;

		/**
		 * Optional failure.
		 */
		private final Throwable failure;

		/**
		 * Expected causes.
		 */
		private final Issue[] causes;

		/**
		 * Instantiate.
		 * 
		 * @param issueDescription
		 *            Issue description.
		 * @param failure
		 *            Optional failure.
		 */
		public Issue(String issueDescription, Throwable failure) {
			this.issueDescription = issueDescription;
			this.failure = failure;
			this.causes = new Issue[0];
		}

		/**
		 * Instantiate.
		 * 
		 * @param issueDescription
		 *            Issue description.
		 * @param causes
		 *            Causes.
		 */
		public Issue(String issueDescription, Issue... causes) {
			this.issueDescription = issueDescription;
			this.failure = null;
			this.causes = causes;
		}
	}

	/**
	 * Mock {@link AbstractCompilerIssues} for testing.
	 */
	private class MockAbstractCompilerIssues extends AbstractCompilerIssues {

		/**
		 * Reported {@link CompilerIssue} instances.
		 */
		private final List<CompilerIssue> reportedIssues = new LinkedList<CompilerIssue>();

		/*
		 * ================ AbstractCompilerIssues ==================
		 */

		@Override
		protected void handleDefaultIssue(DefaultCompilerIssue issue) {
			this.reportedIssues.add(issue);
		}
	}

}