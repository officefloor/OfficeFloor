package net.officefloor.compile.test.issues;

import junit.framework.AssertionFailedError;
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
		this.issues.recordIssue("NODE", this.node.getClass(), "message");
		this.replayMockObjects();
		boolean isSuccessful = false;
		try {
			this.issues.addIssue(this.node, "invalid");
			isSuccessful = true;
		} catch (AssertionFailedError error) {
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
		this.issues.recordIssue("NODE", this.node.getClass(), "failure", new Exception("TEST"));
		this.recordReturn(this.node, this.node.getQualifiedName(), "NODE");
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
		this.issues.recordIssue("NODE", this.node.getClass(), "TEST", new MockCompilerIssue());
		this.replayMockObjects();
		boolean isSuccessful = false;
		try {
			this.issues.addIssue(this.node, "TEST", new MockCompilerIssue());
			isSuccessful = true;
		} catch (AssertionFailedError error) {
		}
		assertFalse("Should not be successful", isSuccessful);
	}

	/**
	 * Mock {@link CompilerIssue}.
	 */
	private static class MockCompilerIssue implements CompilerIssue {
	}

}
