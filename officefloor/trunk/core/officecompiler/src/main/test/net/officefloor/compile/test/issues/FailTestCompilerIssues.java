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
package net.officefloor.compile.test.issues;

import junit.framework.TestCase;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;

/**
 * {@link CompilerIssues} that invokes {@link TestCase#fail()} for issues
 * raised.
 * 
 * @author Daniel Sagenschneider
 */
public class FailTestCompilerIssues extends AbstractCompilerIssues {

	/*
	 * =================== AbstractCompilerIssues ===================
	 */

	@Override
	public void addIssue(Node node, String issueDescription, Throwable cause) {

		// Enable test failures to bubble up
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		}

		// Handle non-test failure issue
		super.addIssue(node, issueDescription, cause);
	}

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		TestCase.fail(new CompileException(issue).toString());
	}

}