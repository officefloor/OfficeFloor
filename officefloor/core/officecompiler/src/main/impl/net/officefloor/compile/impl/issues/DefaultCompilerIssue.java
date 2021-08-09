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

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;

/**
 * Default {@link CompilerIssue}.
 *
 * @author Daniel Sagenschneider
 */
public class DefaultCompilerIssue implements CompilerIssue {

	/**
	 * {@link Node}.
	 */
	private final Node node;

	/**
	 * Issue description.
	 */
	private final String issueDescription;

	/**
	 * Optional cause.
	 */
	private final Throwable cause;

	/**
	 * Optional {@link CompilerIssue} causes.
	 */
	private final CompilerIssue[] causes;

	/**
	 * Instantiate.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Optional cause. May be <code>null</code>.
	 * @param causes
	 *            Optional {@link CompilerIssue} causes.
	 */
	public DefaultCompilerIssue(Node node, String issueDescription,
			Throwable cause, CompilerIssue[] causes) {
		this.node = node;
		this.issueDescription = issueDescription;
		this.cause = cause;
		this.causes = causes;
	}

	/**
	 * Obtains the {@link Node}.
	 * 
	 * @return {@link Node}.
	 */
	public Node getNode() {
		return this.node;
	}

	/**
	 * Obtains the issue description.
	 * 
	 * @return Issue description.
	 */
	public String getIssueDescription() {
		return this.issueDescription;
	}

	/**
	 * Obtains the optional cause.
	 * 
	 * @return Cause and may be <code>null</code>.
	 */
	public Throwable getCause() {
		return this.cause;
	}

	/**
	 * Obtains the optional {@link CompilerIssue} causes.
	 * 
	 * @return Optional {@link CompilerIssue} causes.
	 */
	public CompilerIssue[] getCauses() {
		return this.causes;
	}

}
