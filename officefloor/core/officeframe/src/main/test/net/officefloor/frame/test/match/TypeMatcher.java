package net.officefloor.frame.test.match;

import org.easymock.AbstractMatcher;
import org.junit.Assert;

/**
 * {@link AbstractMatcher} that checks type of objects only.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeMatcher extends AbstractMatcher {

	/**
	 * Types of the arguments expected.
	 */
	protected final Class<?>[] matchTypes;

	/**
	 * Initiate with a multiple argument types.
	 * 
	 * @param types
	 *            Types corresponding the parameters.
	 */
	public TypeMatcher(Class<?>... types) {
		// Initiate state
		this.matchTypes = types;
	}

	/*
	 * ====================== AbstractMatcher =========================
	 */

	@Override
	public boolean matches(Object[] expected, Object[] actual) {

		// Ensure get actual matches
		if (actual == null) {
			Assert.fail("No actual values");
		}

		// Determine if incorrect number of parameters
		if (actual.length != this.matchTypes.length) {
			Assert.fail("Invalid number of parameters configured into "
					+ this.getClass().getSimpleName());
		}

		// Ensure parameters match
		int index = 0;
		for (Class<?> matchType : this.matchTypes) {
			// Ensure null if expected
			if (matchType == null) {
				if (actual[index] != null) {
					return false;
				}
			} else {
				// Ensure same type
				if (!matchType.isInstance(actual[index])) {
					return false;
				}
			}
			index++;
		}

		// Matches
		return true;
	}

}