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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link MockCompilerIssues}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockCompilerIssuesTest extends OfficeFrameTestCase {

	/**
	 * {@link MockCompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * Mock {@link Node}.
	 */
	private final Node node = this.createMock(Node.class);

	/**
	 * Ensure handle default {@link Node}.
	 */
	public void testDefaultNode() {
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, OfficeFloorCompilerImpl.class, "TEST");
		this.replayMockObjects();
		this.issues.addIssue(OfficeFloorCompiler.newOfficeFloorCompiler(null), "TEST");
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle issue.
	 */
	public void testIssue() {
		this.issues.recordIssue("NODE", this.node.getClass(), "TEST");
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.replayMockObjects();
		this.issues.addIssue(this.node, "TEST");
		this.verifyMockObjects();
	}

	/**
	 * Ensure throws failure if invalid description.
	 */
	public void testInvalidIssue() {
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.issues.recordIssue("NODE", this.node.getClass(), "message");
		this.replayMockObjects();
		boolean isSuccessful = false;
		try {
			this.issues.addIssue(this.node, "invalid");
			isSuccessful = true;
		} catch (AssertionError error) {
		}
		assertFalse("Should not be successful", isSuccessful);
	}

	/**
	 * Ensure handle issue regular expression.
	 */
	public void testIssueRegularExpression() {
		this.issues.recordIssueRegex("NODE", this.node.getClass(), "start.+");
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.replayMockObjects();
		this.issues.addIssue(this.node, "start and more ignored description");
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle issue with {@link Exception}.
	 */
	public void testIssueWithException() {
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.issues.recordIssue("NODE", this.node.getClass(), "failure", new Exception("TEST"));
		this.replayMockObjects();
		this.issues.addIssue(this.node, "failure", new Exception("TEST"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle issue regular expression.
	 */
	public void testIssueRegularExpressionWithException() {
		this.issues.recordIssueRegex("NODE", this.node.getClass(), "start.+", new UnsupportedOperationException());
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.replayMockObjects();
		this.issues.addIssue(this.node, "start and more ignored description \n including \n new \n lines",
				new UnsupportedOperationException());
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle {@link IssueCapture}.
	 */
	public void testIssueCapture() {
		MockCompilerIssue issue = new MockCompilerIssue();
		this.issues.recordIssue("NODE", this.node.getClass(), "TEST", issue);
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.replayMockObjects();
		this.issues.addIssue(this.node, "TEST", issue);
		this.verifyMockObjects();
	}

	/**
	 * Ensure throws failure if invalid {@link CompilerIssue}.
	 */
	public void testInvalidIssueCapture() {
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
		this.issues.recordIssue("NODE", this.node.getClass(), "TEST", new MockCompilerIssue());
		this.replayMockObjects();
		boolean isSuccessful = false;
		try {
			this.issues.addIssue(this.node, "TEST", new MockCompilerIssue());
			isSuccessful = true;
		} catch (AssertionError error) {
		}
		assertFalse("Should not be successful", isSuccessful);
	}

	/**
	 * Mock {@link CompilerIssue}.
	 */
	private static class MockCompilerIssue implements CompilerIssue {
	}

}
