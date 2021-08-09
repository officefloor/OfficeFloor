/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;

/**
 * {@link Exception} wrapping the {@link CleanupEscalation} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CleanupException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Print adapter.
	 */
	private static interface PrintAdapter<P> {

		/**
		 * Prints the text.
		 * 
		 * @param text    Text.
		 * @param printer Printer.
		 */
		void print(String text, P printer);

		/**
		 * Prints the text and a new line.
		 * 
		 * @param text    Text.
		 * @param printer Printer.
		 */
		void println(String text, P printer);

		/**
		 * Prints the {@link Throwable} stack trace.
		 * 
		 * @param failure Failure.
		 * @param printer Printer.
		 */
		void print(Throwable failure, P printer);
	}

	/**
	 * {@link PrintStream} {@link PrintAdapter}.
	 */
	private static PrintAdapter<PrintStream> PRINT_STREAM_ADAPTER = new PrintAdapter<PrintStream>() {

		@Override
		public void print(String text, PrintStream printer) {
			printer.print(text);
		}

		@Override
		public void println(String text, PrintStream printer) {
			printer.println(text);
		}

		@Override
		public void print(Throwable failure, PrintStream printer) {
			failure.printStackTrace(printer);
		}
	};

	/**
	 * {@link PrintWriter} {@link PrintAdapter}.
	 */
	private static PrintAdapter<PrintWriter> PRINT_WRITER_ADAPTER = new PrintAdapter<PrintWriter>() {

		@Override
		public void print(String text, PrintWriter printer) {
			printer.print(text);
		}

		@Override
		public void println(String text, PrintWriter printer) {
			printer.println(text);
		}

		@Override
		public void print(Throwable failure, PrintWriter printer) {
			failure.printStackTrace(printer);
		}
	};

	/**
	 * {@link CleanupEscalation} instances.
	 */
	private final CleanupEscalation[] cleanupEscalations;

	/**
	 * Instantiate.
	 * 
	 * @param cleanupEscalations {@link CleanupEscalation} instances.
	 */
	public CleanupException(CleanupEscalation[] cleanupEscalations) {
		this.cleanupEscalations = cleanupEscalations;
	}

	/**
	 * Obtains the {@link CleanupEscalation} instances.
	 * 
	 * @return {@link CleanupEscalation} instances.
	 */
	public CleanupEscalation[] getCleanupEscalations() {
		return this.cleanupEscalations;
	}

	/*
	 * ================= Exception =================
	 */

	@Override
	public void printStackTrace(PrintStream stream) {
		this.printStackTrace(stream, PRINT_STREAM_ADAPTER);
	}

	@Override
	public void printStackTrace(PrintWriter writer) {
		this.printStackTrace(writer, PRINT_WRITER_ADAPTER);
	}

	/**
	 * Prints the stack trace.
	 * 
	 * @param printer Printer.
	 * @param adapter {@link PrintAdapter}.
	 */
	private <P> void printStackTrace(P printer, PrintAdapter<P> adapter) {

		// Print the clean up escalation instances
		for (int i = 0; i < cleanupEscalations.length; i++) {
			CleanupEscalation cleanupEscalation = cleanupEscalations[i];

			// Write the clean up escalation
			adapter.print("Clean up failure with object of type ", printer);
			adapter.println(cleanupEscalation.getObjectType().getName(), printer);
			adapter.print(cleanupEscalation.getEscalation(), printer);
			adapter.println("", printer);
			adapter.println("", printer);
		}
	}

}
