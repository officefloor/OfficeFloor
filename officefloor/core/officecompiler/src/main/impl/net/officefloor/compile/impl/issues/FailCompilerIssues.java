/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.issues;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link CompilerIssues} that fails on an issue.
 * 
 * @author Daniel Sagenschneider
 */
public class FailCompilerIssues extends AbstractCompilerIssues {

	/**
	 * Compiles with exception of first issue.
	 * 
	 * @param compiler        {@link OfficeFloorCompiler}.
	 * @param officeFloorName {@link OfficeFloor} name.
	 * @return {@link OfficeFloor}.
	 * @throws CompileException If fails to compile.
	 */
	public static OfficeFloor compile(OfficeFloorCompiler compiler, String officeFloorName) throws CompileException {

		// Override the compiler issues
		compiler.setCompilerIssues(new FailCompilerIssues());

		// Compile
		try {
			return compiler.compile(officeFloorName);
		} catch (CompileError propagate) {
			throw new CompileException(propagate.issue);
		}
	}

	/*
	 * ======================== AbstractCompilerIssues ========================
	 */

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		throw new CompileError(issue);
	}

	/**
	 * Compile {@link Error} to propagate issue.
	 */
	private static class CompileError extends Error {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link DefaultCompilerIssue}.
		 */
		private final DefaultCompilerIssue issue;

		/**
		 * Initiate.
		 * 
		 * @param issue {@link DefaultCompilerIssue}.
		 */
		public CompileError(DefaultCompilerIssue issue) {
			super(issue.getIssueDescription());
			this.issue = issue;
		}

		/*
		 * ======================= Throwable ============================
		 * 
		 * Mimic compile exception, as may not be handled.
		 */

		@Override
		public String toString() {
			return new CompileException(this.issue).toString();
		}

		@Override
		public void printStackTrace() {
			new CompileException(this.issue).printStackTrace();
		}

		@Override
		public void printStackTrace(PrintStream stream) {
			new CompileException(this.issue).printStackTrace(stream);
		}

		@Override
		public void printStackTrace(PrintWriter writer) {
			new CompileException(this.issue).printStackTrace(writer);
		}

		@Override
		public StackTraceElement[] getStackTrace() {
			return new CompileException(this.issue).getStackTrace();
		}
	}

}
