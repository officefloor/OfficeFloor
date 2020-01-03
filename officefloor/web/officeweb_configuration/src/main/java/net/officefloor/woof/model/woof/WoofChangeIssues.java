package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Allows the WoOF {@link Change} to report an issue when it is
 * applying/reverting.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofChangeIssues {

	/**
	 * Adds an issue.
	 * 
	 * @param message
	 *            Message.
	 */
	void addIssue(String message);

	/**
	 * Adds an issue.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	void addIssue(String message, Throwable cause);

}