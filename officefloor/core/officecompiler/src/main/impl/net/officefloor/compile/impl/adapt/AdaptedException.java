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