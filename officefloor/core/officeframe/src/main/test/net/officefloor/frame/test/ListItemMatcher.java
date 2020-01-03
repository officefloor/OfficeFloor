package net.officefloor.frame.test;

import junit.framework.AssertionFailedError;

/**
 * Matches a list item.
 * 
 * @author Daniel Sagenschneider
 */
public interface ListItemMatcher<O> {

	/**
	 * Specifies whether the list item matches.
	 * 
	 * @param index
	 *            Index of item within the list.
	 * @param expected
	 *            Expected value.
	 * @param actual
	 *            Actual value.
	 * @throws AssertionError
	 *             If error in assertions.
	 * @throws AssertionFailedError
	 *             If fails assertion.
	 */
	void match(int index, O expected, O actual);
	
}
