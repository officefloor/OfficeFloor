/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.test.issues;

import java.util.function.Supplier;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Mock {@link CompilerIssues}.
 *
 * @author Daniel Sagenschneider
 */
public class MockCompilerIssues implements CompilerIssues {

	/**
	 * {@link Mock} object to validate {@link CompilerIssues}.
	 */
	private final Mock mock;

	/*
	 * {@link OfficeFrameTestCase} of current test.
	 */
	private final OfficeFrameTestCase testCase;

	/**
	 * Captured {@link CompilerIssue} instances.
	 */
	private CompilerIssue[] capturedIssues = null;

	/**
	 * Initiate with {@link OfficeFrameTestCase}.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase}.
	 */
	public MockCompilerIssues(OfficeFrameTestCase testCase) {
		this.testCase = testCase;
		this.mock = testCase.createMock(Mock.class);
	}

	/**
	 * Records the capturing the {@link CompilerIssue} instances.
	 * 
	 * @param isIssue
	 *            Flag indicating if there is a {@link CompilerIssue}.
	 * @return Captured {@link CompilerIssue} instances.
	 */
	public CompilerIssue[] recordCaptureIssues(boolean isIssue) {

		// Record capture issues
		this.mock.captureIssues();

		// Provide the issues
		this.capturedIssues = (isIssue ? new CompilerIssue[] { this.testCase.createMock(CompilerIssue.class) }
				: new CompilerIssue[0]);
		return this.capturedIssues;
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName
	 *            Name of the {@link Node}.
	 * @param nodeClass
	 *            {@link Class} of the {@link Node} reporting the issue.
	 * @param issueDescription
	 *            Expected issue description.
	 * @param capturedIssues
	 *            Captured {@link CompilerIssue} instances.
	 */
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			CompilerIssue... capturedIssues) {
		this.mock.addIssue(nodeName, nodeClass, issueDescription, new MockCompilerIssuesArray(capturedIssues));
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName
	 *            Name of the {@link Node}.
	 * @param nodeClass
	 *            {@link Class} of the {@link Node} reporting the issue.
	 * @param issueDescription
	 *            Expected issue description.
	 * @param cause
	 *            Expected cause.
	 */
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			Throwable cause) {
		this.mock.addIssue(nodeName, nodeClass, issueDescription, new MockThrowable(cause));
	}

	/**
	 * Records a top level issue.
	 * 
	 * @param issueDescription
	 *            Expected issue description.
	 * @param capturedIssues
	 *            Captured {@link CompilerIssue} instances.
	 */
	public void recordIssue(String issueDescription, CompilerIssue... capturedIssues) {
		this.recordIssue(OfficeFloorCompiler.TYPE, OfficeFloorCompilerImpl.class, issueDescription, capturedIssues);
	}

	/**
	 * Records a top level issue.
	 * 
	 * @param issueDescription
	 *            Expected issue description.
	 * @param cause
	 *            Expected cause.
	 */
	public void recordIssue(String issueDescription, Throwable cause) {
		this.recordIssue(OfficeFloorCompiler.TYPE, OfficeFloorCompilerImpl.class, issueDescription, cause);
	}

	/*
	 * ================== CompilerIssues ============================
	 */

	@Override
	public <R> IssueCapture<R> captureIssues(Supplier<R> supplier) {

		// Run the capture issues
		this.mock.captureIssues();
		final R returnValue = supplier.get();

		// Return the issues
		return new IssueCapture<R>() {

			@Override
			public R getReturnValue() {
				return returnValue;
			}

			@Override
			public CompilerIssue[] getCompilerIssues() {
				return MockCompilerIssues.this.capturedIssues;
			}
		};
	}

	@Override
	public CompileError addIssue(Node node, String issueDescription, CompilerIssue... causes) {
		String nodeName = node.getNodeName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, issueDescription, new MockCompilerIssuesArray(causes));
		return new CompileError(issueDescription);
	}

	@Override
	public CompileError addIssue(Node node, String issueDescription, Throwable cause) {

		// Determine if assertion failure
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		}

		// Undertake adding the issue
		String nodeName = node.getNodeName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, issueDescription, new MockThrowable(cause));

		// Return the error
		return new CompileError(issueDescription);
	}

	/**
	 * Mock interface for ensuring issues are correct.
	 */
	private static interface Mock {

		/**
		 * Enable recording capturing the {@link CompilerIssue} instances.
		 */
		void captureIssues();

		/**
		 * Enable recording adding an issue for a particular type of node.
		 * 
		 * @param nodeName
		 *            Name of the {@link Node}.
		 * @param nodeClass
		 *            {@link Class} of the {@link Node}.
		 * @param issueDescription
		 *            Expected issue description.
		 * @param capturedIssues
		 *            {@link MockCompilerIssuesArray}.
		 */
		void addIssue(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
				MockCompilerIssuesArray capturedIssues);

		/**
		 * Enable recording adding an issue for a particular type of node.
		 * 
		 * @param nodeName
		 *            Name of the {@link Node}.
		 * @param nodeClass
		 *            {@link Class} of the {@link Node}.
		 * @param issueDescription
		 *            Expected issue description.
		 * @param cause
		 *            Expected cause.
		 */
		void addIssue(String nodeName, Class<? extends Node> nodeclClass, String issuedDescription,
				MockThrowable cause);
	}

	/**
	 * Mock {@link CompilerIssue} array.
	 */
	private class MockCompilerIssuesArray {

		/**
		 * {@link CompilerIssue} array.
		 */
		private final CompilerIssue[] issues;

		/**
		 * Instantiate.
		 * 
		 * @param issues
		 *            {@link CompilerIssue} array.
		 */
		public MockCompilerIssuesArray(CompilerIssue[] issues) {
			this.issues = issues;
		}

		/*
		 * ================ Object ====================
		 */

		@Override
		public boolean equals(Object obj) {

			// Ensure right type
			if (!(obj instanceof MockCompilerIssuesArray)) {
				return false;
			}
			MockCompilerIssuesArray that = (MockCompilerIssuesArray) obj;

			// Ensure the number issues match
			if (this.issues.length != that.issues.length) {
				return false;
			}

			// Ensure the issues match
			for (int i = 0; i < this.issues.length; i++) {
				if (this.issues[i] != that.issues[i]) {
					return false;
				}
			}

			// As here, the same issues
			return true;
		}

		@Override
		public String toString() {
			return CompilerIssue.class.getSimpleName() + "[" + this.issues.length + "]";
		}
	}

	/**
	 * Mock {@link Throwable} cause.
	 */
	private class MockThrowable {

		/**
		 * Cause.
		 */
		private final Throwable cause;

		/**
		 * Instantiate.
		 * 
		 * @param cause
		 *            {@link Throwable} cause.
		 */
		public MockThrowable(Throwable cause) {
			this.cause = cause;
		}

		/*
		 * ================ Object ====================
		 */

		@Override
		public boolean equals(Object obj) {

			// Ensure right type
			if (!(obj instanceof MockThrowable)) {
				return false;
			}
			MockThrowable that = (MockThrowable) obj;

			// Ensure the same type of exception
			if (!this.cause.getClass().equals(that.cause.getClass())) {
				return false;
			}

			// Ensure message is the same
			if (!this.cause.getMessage().equals(that.cause.getMessage())) {
				return false;
			}

			// As here, the same issues
			return true;
		}

		@Override
		public String toString() {
			return this.cause.toString();
		}
	}

}