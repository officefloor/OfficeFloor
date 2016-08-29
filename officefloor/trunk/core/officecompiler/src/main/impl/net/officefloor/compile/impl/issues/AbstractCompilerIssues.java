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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;

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
	public CompilerIssue[] captureIssues(Runnable runnable) {
		// Create the context for this capture
		List<CompilerIssue> invocationContext = new LinkedList<CompilerIssue>();
		this.context.push(invocationContext);
		try {

			// Undertake functionality within capture context
			runnable.run();

		} finally {
			// Ensure pop the context
			this.context.pop();
		}
		return invocationContext.toArray(new CompilerIssue[invocationContext
				.size()]);
	}

	@Override
	public void addIssue(Node node, String issueDescription,
			CompilerIssue... causes) {
		this.addIssue(this.createCompilerIssue(node, issueDescription, null,
				causes));
	}

	@Override
	public void addIssue(Node node, String issueDescription, Throwable cause) {
		this.addIssue(this.createCompilerIssue(node, issueDescription, cause,
				new DefaultCompilerIssue[0]));
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
	protected CompilerIssue createCompilerIssue(Node node,
			String issueDescription, Throwable cause, CompilerIssue[] causes) {
		return new DefaultCompilerIssue(node, issueDescription, cause, causes);
	}

}