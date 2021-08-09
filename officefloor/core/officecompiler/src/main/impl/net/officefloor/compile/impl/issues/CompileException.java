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

package net.officefloor.compile.impl.issues;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;

/**
 * Compile {@link Exception}.
 *
 * @author Daniel Sagenschneider
 */
public class CompileException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Obtains the {@link DefaultCompilerIssue} as string.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 * @return Text details of the {@link DefaultCompilerIssue}.
	 */
	public static String toIssueString(DefaultCompilerIssue issue) {
		StringWriter message = new StringWriter();
		printIssue(issue, new PrintWriter(message));
		return message.toString();
	}

	/**
	 * Prints the {@link DefaultCompilerIssue} to the {@link PrintStream}.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 * @param out   {@link PrintStream}.
	 */
	public static void printIssue(DefaultCompilerIssue issue, PrintStream out) {
		printIssue(issue, new PrintStreamOutput(out), 0);
	}

	/**
	 * Prints the {@link DefaultCompilerIssue} to the {@link PrintWriter}.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 * @param out   {@link PrintWriter}.
	 */
	public static void printIssue(DefaultCompilerIssue issue, PrintWriter out) {
		printIssue(issue, new PrintWriterOutput(out), 0);
	}

	/**
	 * {@link DefaultCompilerIssue}.
	 */
	private DefaultCompilerIssue issue;

	/**
	 * Instantiate.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 */
	public CompileException(DefaultCompilerIssue issue) {
		super(issue.getIssueDescription());
		this.issue = issue;
	}

	/**
	 * Obtains the {@link CompilerIssue}.
	 * 
	 * @return {@link CompilerIssue}.
	 */
	public DefaultCompilerIssue getCompilerIssue() {
		return this.issue;
	}

	/*
	 * ======================= Throwable ============================
	 */

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		printIssue(this.issue, new PrintWriter(buffer));
		return buffer.toString();
	}

	@Override
	public void printStackTrace() {
		printIssue(this.issue, new PrintStreamOutput(System.err), 0);
	}

	@Override
	public void printStackTrace(PrintStream stream) {
		printIssue(this.issue, new PrintStreamOutput(stream), 0);
	}

	@Override
	public void printStackTrace(PrintWriter writer) {
		printIssue(this.issue, new PrintWriterOutput(writer), 0);
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		List<StackTraceElement> nodeTrace = new LinkedList<StackTraceElement>();
		fillNodeTrace(this.issue.getNode(), nodeTrace);
		return nodeTrace.toArray(new StackTraceElement[nodeTrace.size()]);
	}

	/*
	 * ======================= (helper) ============================
	 */

	/**
	 * {@link Output} to enable writing to either {@link PrintStream} or
	 * {@link PrintWriter}.
	 */
	private static interface Output {

		/**
		 * Prints the text.
		 * 
		 * @param text Text.
		 */
		void print(String text);

		/**
		 * Prints the stack trace.
		 * 
		 * @param throwable {@link Throwable}.
		 */
		void printStackTrace(Throwable throwable);

		/**
		 * Prints new line.
		 */
		void println();
	}

	/**
	 * {@link PrintStream} {@link Output}.
	 */
	private static class PrintStreamOutput implements Output {

		/**
		 * {@link PrintStream}.
		 */
		private final PrintStream stream;

		/**
		 * Initiate.
		 * 
		 * @param stream {@link PrintStream}.
		 */
		public PrintStreamOutput(PrintStream stream) {
			this.stream = stream;
		}

		@Override
		public void print(String text) {
			this.stream.print(text);
		}

		@Override
		public void printStackTrace(Throwable throwable) {
			throwable.printStackTrace(this.stream);
		}

		@Override
		public void println() {
			this.stream.println();
		}
	}

	/**
	 * {@link PrintWriter} {@link Output}.
	 */
	private static class PrintWriterOutput implements Output {

		/**
		 * {@link PrintWriter}.
		 */
		private final PrintWriter writer;

		/**
		 * Initiate.
		 * 
		 * @param writer {@link PrintWriter}.
		 */
		public PrintWriterOutput(PrintWriter writer) {
			this.writer = writer;
		}

		@Override
		public void print(String text) {
			this.writer.print(text);
		}

		@Override
		public void printStackTrace(Throwable throwable) {
			throwable.printStackTrace(this.writer);
		}

		@Override
		public void println() {
			this.writer.println();
		}
	}

	/**
	 * Prints the {@link CompilerIssue}.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 * @param out   {@link Output}.
	 * @param depth Depth into the causes.
	 */
	private static void printIssue(DefaultCompilerIssue issue, Output out, int depth) {

		// Output details of issue
		out.print(issue.getIssueDescription());
		Throwable cause = issue.getCause();
		if (cause != null) {
			printLineSeparation(out, depth);
			out.printStackTrace(cause);
		}
		printLineSeparation(out, depth);
		printNodeTrace(issue.getNode(), out, depth);
		printLineSeparation(out, depth);
		printCauses(issue, out, depth);
	}

	/**
	 * Prints the {@link CompilerIssue} causes.
	 * 
	 * @param issue {@link DefaultCompilerIssue}.
	 * @param out   {@link Output}.
	 * @param depth Depth into the causes.
	 */
	private static void printCauses(DefaultCompilerIssue issue, Output out, int depth) {

		// Obtain the causes
		CompilerIssue[] causes = issue.getCauses();
		if (causes == null) {
			return; // no causes, so nothing to print
		}

		// Print the cause
		for (int i = 0; i < causes.length; i++) {
			DefaultCompilerIssue cause = (DefaultCompilerIssue) causes[i];

			// Determine first cause (or subsequent causes)
			if (i == 0) {
				out.print("Caused by:");
			} else {
				out.print("And also caused by:");
			}
			printIssue(cause, out, depth + 1);
		}
	}

	/**
	 * Prints the separation between the lines, taking into account depth into
	 * causes.
	 * 
	 * @param out   {@link Output}.
	 * @param depth Depth into the causes.
	 */
	private static void printLineSeparation(Output out, int depth) {

		// Provide new line
		out.println();

		// Provide indent
		for (int i = 0; i < (depth * 2); i++) {
			out.print(" ");
		}
	}

	/**
	 * Loads the {@link Node} trace.
	 * 
	 * @param node  {@link Node}.
	 * @param out   {@link Output}.
	 * @param depth Depth into the causes.
	 */
	private static void printNodeTrace(Node node, Output out, int depth) {

		// Stop if no node
		if (node == null) {
			return;
		}

		// Load the entry for the node
		out.print(" - ");
		out.print(node.getNodeName());
		out.print(" [");
		out.print(node.getNodeType());
		out.print("]");
		String location = node.getLocation();
		if (location != null) {
			out.print(" @ ");
			out.print(location);
		}
		printLineSeparation(out, depth);

		// Load parent
		printNodeTrace(node.getParentNode(), out, depth);
	}

	/**
	 * Fills the {@link Node} trace.
	 * 
	 * @param node  {@link Node}.
	 * @param trace Trace to fill.
	 */
	private static void fillNodeTrace(Node node, List<StackTraceElement> trace) {

		// Stop if no node
		if (node == null) {
			return;
		}

		// Create the stack trace element
		StackTraceElement element = new StackTraceElement(node.getNodeType(), node.getNodeName(), node.getLocation(),
				0);

		// Add the to trace
		trace.add(element);

		// Load next node in trace
		fillNodeTrace(node.getParentNode(), trace);
	}

}
