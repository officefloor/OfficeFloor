package net.officefloor.frame.test.match;

/**
 * Arguments matcher.
 * 
 * @author Daniel Sagenschneider
 */
public interface ArgumentsMatcher {

	/**
	 * Indicates if the arguments are as expected.
	 * 
	 * @param actual Arguments.
	 * @return If matches.
	 */
	boolean matches(Object[] actual);

}