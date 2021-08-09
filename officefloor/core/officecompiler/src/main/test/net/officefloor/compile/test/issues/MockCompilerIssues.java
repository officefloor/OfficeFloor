/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.test.issues;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.frame.test.MockTestSupport;
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
	 * {@link MockTestSupport} of current test.
	 */
	private final MockTestSupport mockTestSupport;

	/**
	 * Captured {@link CompilerIssue} instances.
	 */
	private CompilerIssue[] capturedIssues = null;

	/**
	 * Initiate with {@link OfficeFrameTestCase}.
	 * 
	 * @param testCase {@link OfficeFrameTestCase}.
	 */
	public MockCompilerIssues(OfficeFrameTestCase testCase) {
		this(testCase.mockTestSupport);
	}

	/**
	 * Initiate with {@link MockTestSupport}.
	 * 
	 * @param mockTestSupport {@link MockTestSupport}.
	 */
	public MockCompilerIssues(MockTestSupport mockTestSupport) {
		this.mockTestSupport = mockTestSupport;
		this.mock = mockTestSupport.createMock(Mock.class);
	}

	/**
	 * Records the capturing the {@link CompilerIssue} instances.
	 * 
	 * @param isIssue Flag indicating if there is a {@link CompilerIssue}.
	 * @return Captured {@link CompilerIssue} instances.
	 */
	public CompilerIssue[] recordCaptureIssues(boolean isIssue) {

		// Record capture issues
		this.mock.captureIssues();

		// Provide the issues
		this.capturedIssues = (isIssue ? new CompilerIssue[] { this.mockTestSupport.createMock(CompilerIssue.class) }
				: new CompilerIssue[0]);
		return this.capturedIssues;
	}

	/**
	 * Records a number of capturing the {@link CompilerIssue} instances.
	 * 
	 * @param repetitions Number of repetitions.
	 */
	public void recordCaptureIssues_repeated(int repetitions) {
		for (int i = 0; i < repetitions; i++) {
			this.recordCaptureIssues(false);
		}
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName         Name of the {@link Node}.
	 * @param nodeClass        {@link Class} of the {@link Node} reporting the
	 *                         issue.
	 * @param issueDescription Expected issue description.
	 * @param capturedIssues   Captured {@link CompilerIssue} instances.
	 */
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			CompilerIssue... capturedIssues) {
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, false),
				new MockCompilerIssuesArray(capturedIssues));
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName         Name of the {@link Node}.
	 * @param nodeClass        {@link Class} of the {@link Node} reporting the
	 *                         issue.
	 * @param issueDescription Expected issue description.
	 * @param cause            Expected cause.
	 */
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			Throwable cause) {
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, false),
				new MockThrowable(cause));
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName         Name of the {@link Node}.
	 * @param nodeClass        {@link Class} of the {@link Node} reporting the
	 *                         issue.
	 * @param issueDescription Expected issue description as regular expression.
	 * @param capturedIssues   Captured {@link CompilerIssue} instances.
	 */
	public void recordIssueRegex(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			CompilerIssue... capturedIssues) {
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, true),
				new MockCompilerIssuesArray(capturedIssues));
	}

	/**
	 * Records an issue against a {@link Node}.
	 * 
	 * @param nodeName         Name of the {@link Node}.
	 * @param nodeClass        {@link Class} of the {@link Node} reporting the
	 *                         issue.
	 * @param issueDescription Expected issue description as regular expression.
	 * @param cause            Expected cause.
	 */
	public void recordIssueRegex(String nodeName, Class<? extends Node> nodeClass, String issueDescription,
			Throwable cause) {
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, true),
				new MockThrowable(cause));
	}

	/**
	 * Records a top level issue.
	 * 
	 * @param issueDescription Expected issue description.
	 * @param capturedIssues   Captured {@link CompilerIssue} instances.
	 */
	public void recordIssue(String issueDescription, CompilerIssue... capturedIssues) {
		this.recordIssue(OfficeFloorCompiler.TYPE, OfficeFloorCompilerImpl.class, issueDescription, capturedIssues);
	}

	/**
	 * Records a top level issue.
	 * 
	 * @param issueDescription Expected issue description.
	 * @param cause            Expected cause.
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
		String nodeName = node.getQualifiedName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, false),
				new MockCompilerIssuesArray(causes));
		return new CompileError(issueDescription);
	}

	@Override
	public CompileError addIssue(Node node, String issueDescription, Throwable cause) {

		// Determine if assertion failure
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		}

		// Undertake adding the issue
		String nodeName = node.getQualifiedName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, new MockIssueDescription(issueDescription, false),
				new MockThrowable(cause));

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
		 * @param nodeName         Name of the {@link Node}.
		 * @param nodeClass        {@link Class} of the {@link Node}.
		 * @param issueDescription Expected issue description.
		 * @param capturedIssues   {@link MockCompilerIssuesArray}.
		 */
		void addIssue(String nodeName, Class<? extends Node> nodeClass, MockIssueDescription issueDescription,
				MockCompilerIssuesArray capturedIssues);

		/**
		 * Enable recording adding an issue for a particular type of node.
		 * 
		 * @param nodeName         Name of the {@link Node}.
		 * @param nodeClass        {@link Class} of the {@link Node}.
		 * @param issueDescription Expected issue description.
		 * @param cause            Expected cause.
		 */
		void addIssue(String nodeName, Class<? extends Node> nodeclClass, MockIssueDescription issuedDescription,
				MockThrowable cause);
	}

	/**
	 * Mock {@link CompilerIssue} description.
	 */
	private class MockIssueDescription {

		/**
		 * Description.
		 */
		private final String description;

		/**
		 * Indicates if regular expression match.
		 */
		private final boolean isRegularExpression;

		/**
		 * Instantiate.
		 * 
		 * @param description         {@link CompilerIssue} description.
		 * @param isRegularExpression Indicates if description is regular expression
		 *                            match.
		 */
		public MockIssueDescription(String description, boolean isRegularExpression) {
			this.description = description;
			this.isRegularExpression = isRegularExpression;
		}

		/*
		 * ================ Object ====================
		 */

		@Override
		public boolean equals(Object obj) {

			// Ensure right type
			if (!(obj instanceof MockIssueDescription)) {
				return false;
			}
			MockIssueDescription that = (MockIssueDescription) obj;

			// Determine if regular
			if (this.isRegularExpression || that.isRegularExpression) {

				// Ensure both are not a regular expression
				if (this.isRegularExpression && that.isRegularExpression) {
					return false;
				}

				// Obtain the regular expression
				MockIssueDescription check = this;
				if (!check.isRegularExpression) {
					// Swap around (as other is regular expression)
					check = that;
					that = this;
				}
				Pattern pattern = Pattern.compile(check.description, Pattern.DOTALL);

				// Return if matches pattern
				return pattern.matcher(that.description).matches();
			}

			// As here, compare only on description
			return Objects.equals(this.description, that.description);
		}

		@Override
		public String toString() {
			return this.description + (this.isRegularExpression ? " (RegEx)" : "");
		}
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
		 * @param issues {@link CompilerIssue} array.
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
		 * @param cause {@link Throwable} cause.
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
			String thisMessage = this.cause.getMessage();
			String thatMessage = that.cause.getMessage();
			if (thisMessage == null) {
				return (thatMessage == null);
			} else {
				return thisMessage.equals(thatMessage);
			}
		}

		@Override
		public String toString() {
			return this.cause.toString();
		}
	}

}
