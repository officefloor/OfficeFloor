package net.officefloor.frame.test.match;

import java.util.regex.Matcher;

import org.easymock.internal.AlwaysMatcher;

/**
 * {@link Matcher} that provides stub functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class StubMatcher extends AlwaysMatcher {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.easymock.internal.AlwaysMatcher#matches(java.lang.Object[],
	 * java.lang.Object[])
	 */
	@Override
	public boolean matches(Object[] expected, Object[] actual) {
		this.stub(actual);
		return true;
	}

	/**
	 * Override to provide stub functionality.
	 * 
	 * @param arguments
	 *            Arguments to method.
	 */
	protected abstract void stub(Object[] arguments);

}
