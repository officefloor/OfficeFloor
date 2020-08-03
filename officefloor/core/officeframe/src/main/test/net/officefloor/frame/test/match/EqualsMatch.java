package net.officefloor.frame.test.match;

/**
 * Equals {@link AbstractMatch}.
 * 
 * @author Daniel Sagenschneider
 */
public class EqualsMatch extends AbstractMatch {

	/**
	 * Instantiate.
	 * 
	 * @param expected Expected.
	 */
	public EqualsMatch(Object expected) {
		super(expected);
	}

	/*
	 * ================== AbstractMatch =======================
	 */

	@Override
	public boolean isMatch(Object actual) {
		return this.expected.equals(actual);
	}

}
