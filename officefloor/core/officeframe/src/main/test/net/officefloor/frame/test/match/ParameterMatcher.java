package net.officefloor.frame.test.match;

import org.easymock.AbstractMatcher;

/**
 * {@link AbstractMatcher}
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterMatcher extends AbstractMatcher {

	/**
	 * {@link Match} that provides <code>equals</code>
	 */
	public static Match equals = new Match() {
		@Override
		public boolean isMatch(Object expected, Object actual) {
			return expected.equals(actual);
		}
	};

	/**
	 * {@link Match} that provides <code>type</code> matching.
	 */
	public static Match type = new Match() {
		@Override
		public boolean isMatch(Object expected, Object actual) {
			return expected.getClass().isInstance(actual);
		}
	};

	/**
	 * {@link Match} instances for matching the parameters.
	 */
	private final Match[] matches;

	/**
	 * Initiate.
	 * 
	 * @param matches
	 *            {@link Match} instances for matching the parameters.
	 */
	public ParameterMatcher(Match... matches) {
		this.matches = matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.easymock.AbstractMatcher#matches(java.lang.Object[],
	 *      java.lang.Object[])
	 */
	@Override
	public boolean matches(Object[] expected, Object[] actual) {

		// Ensure correct number of expected matches
		if (this.matches.length != expected.length) {
			return false;
		}

		// Ensure correct number of actual parameters
		if (this.matches.length != actual.length) {
			return false;
		}

		// Ensure parameters match
		for (int i = 0; i < this.matches.length; i++) {
			if (!this.matches[i].isMatch(expected[i], actual[i])) {
				return false;
			}
		}

		// Matches if at this point
		return true;
	}
}
