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

package net.officefloor.compile.issues;

/**
 * Provides means to raise {@link CompilerIssue}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceIssues {

	/**
	 * <p>
	 * Allows the source to add an issue.
	 * <p>
	 * This is available to report invalid configuration.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @return {@link CompileError} to be used in <code>throw</code> statement
	 *         when adding {@link CompilerIssue} to avoid further compiling.
	 */
	CompileError addIssue(String issueDescription);

	/**
	 * <p>
	 * Allows the source to add an issue along with its cause.
	 * <p>
	 * This is available to report invalid configuration.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @return {@link CompileError} to be used in <code>throw</code> statement
	 *         when adding {@link CompilerIssue} to avoid further compiling.
	 */
	CompileError addIssue(String issueDescription, Throwable cause);

}
