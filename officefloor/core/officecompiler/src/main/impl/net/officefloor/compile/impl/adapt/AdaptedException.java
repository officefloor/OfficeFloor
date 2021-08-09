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

package net.officefloor.compile.impl.adapt;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.officefloor.compile.issues.CompilerIssues;

/**
 * Adapted {@link Exception} to enable details of the cause to be provided to
 * the {@link CompilerIssues} for an issue.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Stack trace.
	 */
	private final String stackTrace;

	/**
	 * Initiate.
	 * 
	 * @param message    Message with adapted details of the cause.
	 * @param stackTrace Stack trace to adapt.
	 */
	public AdaptedException(String message, String stackTrace) {
		super(message);
		this.stackTrace = stackTrace;
	}

	/*
	 * ======================= Throwable ============================
	 */

	@Override
	public String toString() {
		return this.stackTrace;
	}

	@Override
	public void printStackTrace() {
		System.err.println(this.stackTrace);
	}

	@Override
	public void printStackTrace(PrintStream stream) {
		stream.println(this.stackTrace);
	}

	@Override
	public void printStackTrace(PrintWriter writer) {
		writer.println(this.stackTrace);
	}

}
