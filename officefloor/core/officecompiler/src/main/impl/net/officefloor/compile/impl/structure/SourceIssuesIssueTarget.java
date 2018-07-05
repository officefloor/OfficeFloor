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