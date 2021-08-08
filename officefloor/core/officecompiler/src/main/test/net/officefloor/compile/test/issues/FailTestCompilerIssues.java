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

import junit.framework.TestCase;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.test.Assertions;

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
	public CompileError addIssue(Node node, String issueDescription, Throwable cause) {

		// Enable test failures to bubble up
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		}

		// Handle non-test failure issue
		return super.addIssue(node, issueDescription, cause);
	}

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		Assertions.fail(new CompileException(issue).toString());
	}

}
