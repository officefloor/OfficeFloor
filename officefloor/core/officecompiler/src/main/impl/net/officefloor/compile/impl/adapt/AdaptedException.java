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
