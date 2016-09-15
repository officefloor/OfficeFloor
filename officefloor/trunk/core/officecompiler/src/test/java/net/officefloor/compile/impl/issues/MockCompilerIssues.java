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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
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
		this.capturedIssues = (isIssue ? new CompilerIssue[] { this.testCase
				.createMock(CompilerIssue.class) } : new CompilerIssue[0]);
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
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass,
			String issueDescription, CompilerIssue... capturedIssues) {
		this.mock.addIssue(nodeName, nodeClass, issueDescription);
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
	public void recordIssue(String nodeName, Class<? extends Node> nodeClass,
			String issueDescription, Throwable cause) {
		this.mock.addIssue(nodeName, nodeClass, issueDescription, cause);
	}

	/**
	 * Records a top level issue.
	 * 
	 * @param issueDescription
	 *            Expected issue description.
	 * @param capturedIssues
	 *            Captured {@link CompilerIssue} instances.
	 */
	public void recordIssue(String issueDescription,
			CompilerIssue... capturedIssues) {
		this.recordIssue(OfficeFloorCompiler.TYPE,
				OfficeFloorCompilerImpl.class, issueDescription, capturedIssues);
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
		this.recordIssue(OfficeFloorCompiler.TYPE,
				OfficeFloorCompilerImpl.class, issueDescription, cause);
	}

	/*
	 * ================== CompilerIssues ============================
	 */

	@Override
	public CompilerIssue[] captureIssues(Runnable runnable) {

		// Run the capture issues
		this.mock.captureIssues();
		runnable.run();

		// Return the issues
		return this.capturedIssues;
	}

	@Override
	public void addIssue(Node node, String issueDescription,
			CompilerIssue... causes) {
		String nodeName = node.getNodeName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, issueDescription, causes);
	}

	@Override
	public void addIssue(Node node, String issueDescription, Throwable cause) {
		String nodeName = node.getNodeName();
		Class<? extends Node> nodeClass = node.getClass();
		this.mock.addIssue(nodeName, nodeClass, issueDescription, cause);
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
		 *            Captured {@link CompilerIssue} instances.
		 */
		void addIssue(String nodeName, Class<? extends Node> nodeClass,
				String issueDescription, CompilerIssue... capturedIssues);

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
		void addIssue(String nodeName, Class<? extends Node> nodeclClass,
				String issuedDescription, Throwable cause);
	}

}