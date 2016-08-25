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
package net.officefloor.compile.issues;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Notified of issues in compilation of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompilerIssues {

	/**
	 * Captures {@link CompilerIssue} instances.
	 * 
	 * @param runnable
	 *            {@link Runnable} of code to capture {@link CompilerIssue}
	 *            within.
	 * @return Array of {@link CompilerIssue} instances.
	 */
	CompilerIssue[] captureIssues(Runnable runnable);

	/**
	 * Adds an issue regarding a particular {@link Node}.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param causes
	 *            Possible {@link CompilerIssue} instances causing this issue.
	 * @throws Error
	 *             If fail compile fast.
	 * 
	 * @see CompilerIssues#captureIssues(Runnable)
	 */
	void addIssue(Node node, String issueDescription, CompilerIssue... causes);

	/**
	 * Adds an issue regarding a particular {@link Node}.
	 * 
	 * @param node
	 *            {@link Node}.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @throws Error
	 *             If fail compile fast.
	 */
	void addIssue(Node node, String issueDescription, Throwable cause);

}