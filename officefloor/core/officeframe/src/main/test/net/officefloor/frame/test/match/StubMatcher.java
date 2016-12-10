/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
