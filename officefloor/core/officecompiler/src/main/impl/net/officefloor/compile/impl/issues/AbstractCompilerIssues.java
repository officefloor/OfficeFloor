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

package net.officefloor.compile.impl.issues;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;

/**
 * Abstract {@link CompilerIssues}
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCompilerIssues implements CompilerIssues {

	/**
	 * Context for {@link CompilerIssue} instances being added.
	 */
	private final Deque<List<CompilerIssue>> context = new LinkedList<List<CompilerIssue>>();

	/**
	 * Override to handle the {@link CompilerIssue}.
	 * 
	 * @param issue
	 *            {@link CompilerIssue}.
	 */
	protected void handleIssue(CompilerIssue issue) {
		this.handleDefaultIssue((DefaultCompilerIssue) issue);
	}

	/**
	 * Implement to handle {@link DefaultCompilerIssue}.
	 * 
	 * @param issue
	 *            {@link DefaultCompilerIssue}.
	 */
	protected abstract void handleDefaultIssue(DefaultCompilerIssue issue);

	/*
	 * =================== CompilerIssues =================================
	 */

	@Override
	public <R> IssueCapture<R> captureIssues(Supplier<R> supplier) {

		// Create the context for this capture
		List<CompilerIssue> invocationContext = new LinkedList<CompilerIssue>();
		this.context.push(invocationContext);
		final R returnValue;
		try {

			// Undertake functionality within capture context
			returnValue = supplier.get();

		} finally {
			// Ensure pop the context
			this.context.pop();
		}

		// Obtain the possible captured issues
		final CompilerIssue[] issues = invocationContext.toArray(new CompilerIssue[invocationContext.size()]);

		// Return the issue capture
		return new IssueCapture<R>() {

			@Override
			public R getReturnValue() {
				return returnValue;
			}

			@Override
			public CompilerIssue[] getCompilerIssues() {
				return issues;
			}
		};
	}

	@Override
	public CompileError addIssue(Node node, String issueDescription, CompilerIssue... causes) {
		this.addIssue(this.createCompilerIssue(node, issueDescription, null, causes));
		return new CompileError(issueDescription);
	}

	@Override
	public CompileError addIssue(Node node, String issueDescription, Throwable cause) {
		this.addIssue(this.createCompilerIssue(node, issueDescription, cause, new DefaultCompilerIssue[0]));
		return new CompileError(issueDescription);
	}

	/**
	 * Adds the {@link CompilerIssue}.
	 * 
	 * @param issue
	 *            {@link CompilerIssue}.
	 */
	private void addIssue(CompilerIssue issue) {
		// Determine if within capture context
		if (this.context.size() > 0) {
			// Add to current context
			this.context.getFirst().add(issue);

		} else {
			// No context, so handle immediately
			this.handleIssue(issue);
		}
	}

	/*
	 * ================== Override methods =============================
	 */

	/**
	 * Creates the {@link CompilerIssue} for use in abstract functionality. If
	 * not overriden, a {@link DefaultCompilerIssue} is used.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Optional cause. May be <code>null</code>.
	 * @param causes
	 *            Optional cause {@link CompilerIssue} instances.
	 * @return {@link CompilerIssue}, with default implementation providing a
	 *         {@link DefaultCompilerIssue}.
	 */
	protected CompilerIssue createCompilerIssue(Node node, String issueDescription, Throwable cause,
			CompilerIssue[] causes) {
		return new DefaultCompilerIssue(node, issueDescription, cause, causes);
	}

}
