package net.officefloor.frame.test.match;

/**
 * Match interface for a parameter of the {@link ParameterMatcher}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Match {

	/**
	 * Flags whether matches.
	 * 
	 * @param expected
	 *            Expected.
	 * @param actual
	 *            Actual.
	 * @return <code>true</code> if matches.
	 */
	boolean isMatch(Object expected, Object actual);
}
