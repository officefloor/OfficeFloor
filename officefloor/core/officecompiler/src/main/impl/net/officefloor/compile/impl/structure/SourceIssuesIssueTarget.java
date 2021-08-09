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

package net.officefloor.compile.impl.structure;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.frame.api.source.IssueTarget;

/**
 * Adapts the {@link SourceIssues} to {@link IssueTarget}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceIssuesIssueTarget implements IssueTarget {

	/**
	 * {@link SourceIssues}.
	 */
	private final SourceIssues sourceIssues;

	/**
	 * Instantiate.
	 * 
	 * @param sourceIssues
	 *            {@link SourceIssues}.
	 */
	public SourceIssuesIssueTarget(SourceIssues sourceIssues) {
		this.sourceIssues = sourceIssues;
	}

	/*
	 * =================== IssueTarget =====================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.sourceIssues.addIssue(issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.sourceIssues.addIssue(issueDescription, cause);
	}

}
