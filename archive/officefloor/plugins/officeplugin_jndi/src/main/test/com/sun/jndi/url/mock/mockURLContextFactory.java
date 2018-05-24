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
package com.sun.jndi.url.mock;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

/**
 * Mock URL Context Factory for testing,
 * 
 * @author Daniel Sagenschneider
 */
public class mockURLContextFactory implements ObjectFactory,
		InitialContextFactory {

	/**
	 * {@link Context} to be created.
	 */
	private static Context context;

	/**
	 * {@link ContextEnvironmentValidator} instances.
	 */
	private final static List<ContextEnvironmentValidator> validators = new LinkedList<ContextEnvironmentValidator>();

	/**
	 * Specifies the {@link Context}.
	 * 
	 * @param context
	 *            {@link Context}.
	 */
	public static synchronized void setContext(Context context) {
		mockURLContextFactory.context = context;
	}

	/**
	 * Adds a {@link ContextEnvironmentValidator}.
	 * 
	 * @param validator
	 *            {@link ContextEnvironmentValidator}.
	 */
	public static synchronized void addValidator(
			ContextEnvironmentValidator validator) {
		validators.add(validator);
	}

	/**
	 * Resets for another test.
	 */
	public static synchronized void reset() {
		context = null;
		validators.clear();
	}

	/*
	 * ======================= ObjectFactory ===========================
	 */

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {
		return this.getInitialContext(environment);
	}

	/*
	 * =================== InitialContextFactory =========================
	 */

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {
		synchronized (mockURLContextFactory.class) {

			// Run the validators
			for (ContextEnvironmentValidator validator : validators) {
				validator.validateEnvironment(environment);
			}

			// Return the context
			return context;
		}
	}

}