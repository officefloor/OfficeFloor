package net.officefloor.frame.test.match;

/**
 * Type {@link AbstractMatch}.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeMatch extends AbstractMatch {

	/**
	 * Instantiate.
	 * 
	 * @param expected Expected.
	 */
	public TypeMatch(Object expected) {
		super(expected);
	}

	/*
	 * ================ AbstractMatch ====================
	 */

	@Override
	public boolean isMatch(Object actual) {
		return this.expected.getClass().isInstance(actual);
	}

}
