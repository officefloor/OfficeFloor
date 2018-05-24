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
package net.officefloor.plugin.jndi.context;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link Context}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorContext implements Context {

	/**
	 * Schema for {@link OfficeFloor} JNDI context.
	 */
	public static final String SCHEMA = "officefloor";

	/**
	 * {@link ObjectFactory} to create the {@link OfficeFloor} instances.
	 */
	private static final OfficeFloorObjectFactory objectFactory = new OfficeFloorObjectFactory();

	/*
	 * ======================= Context ============================
	 */

	@Override
	public Object lookup(Name name) throws NamingException {
		try {

			// Obtain the OfficeFloor
			Object officeFloor = objectFactory.getObjectInstance(null, name,
					this, null);

			// Return the OfficeFloor
			return officeFloor;

		} catch (Exception ex) {
			// Propagate as naming exception
			if (ex instanceof NamingException) {
				throw (NamingException) ex;
			} else {
				NamingException exception = new NamingException(ex.getMessage());
				exception.setRootCause(ex);
				throw exception;
			}
		}
	}

	@Override
	public Object lookup(String name) throws NamingException {

		// Ensure have name
		if (name == null) {
			return null; // no object if no name
		}

		// Strip off the schema
		int schemaEndIndex = name.indexOf(':');
		if (schemaEndIndex >= 0) {
			name = name.substring(schemaEndIndex + ".".length());
		}

		// Ensure non-blank name provided
		if (name.trim().length() == 0) {
			return null; // no object if no name
		}

		// Create the name object
		Name nameObject = new CompositeName(name);

		// Return the OfficeFloor
		return this.lookup(nameObject);
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		// TODO implement Context.addToEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.addToEnvironment");
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		// TODO implement Context.getEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.getEnvironment");
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		// TODO implement Context.removeFromEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.removeFromEnvironment");
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		// TODO implement Context.bind
		throw new UnsupportedOperationException("TODO implement Context.bind");
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		// TODO implement Context.bind
		throw new UnsupportedOperationException("TODO implement Context.bind");
	}

	@Override
	public void close() throws NamingException {
		// TODO implement Context.close
		throw new UnsupportedOperationException("TODO implement Context.close");
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		// TODO implement Context.composeName
		throw new UnsupportedOperationException(
				"TODO implement Context.composeName");
	}

	@Override
	public String composeName(String name, String prefix)
			throws NamingException {
		// TODO implement Context.composeName
		throw new UnsupportedOperationException(
				"TODO implement Context.composeName");
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		// TODO implement Context.createSubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.createSubcontext");
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		// TODO implement Context.createSubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.createSubcontext");
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		// TODO implement Context.destroySubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.destroySubcontext");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		// TODO implement Context.destroySubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.destroySubcontext");
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		// TODO implement Context.getNameInNamespace
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameInNamespace");
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		// TODO implement Context.getNameParser
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameParser");
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		// TODO implement Context.getNameParser
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameParser");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		// TODO implement Context.list
		throw new UnsupportedOperationException("TODO implement Context.list");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		// TODO implement Context.list
		throw new UnsupportedOperationException("TODO implement Context.list");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		// TODO implement Context.listBindings
		throw new UnsupportedOperationException(
				"TODO implement Context.listBindings");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		// TODO implement Context.listBindings
		throw new UnsupportedOperationException(
				"TODO implement Context.listBindings");
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		// TODO implement Context.lookupLink
		throw new UnsupportedOperationException(
				"TODO implement Context.lookupLink");
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		// TODO implement Context.lookupLink
		throw new UnsupportedOperationException(
				"TODO implement Context.lookupLink");
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		// TODO implement Context.rebind
		throw new UnsupportedOperationException("TODO implement Context.rebind");
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		// TODO implement Context.rebind
		throw new UnsupportedOperationException("TODO implement Context.rebind");
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		// TODO implement Context.rename
		throw new UnsupportedOperationException("TODO implement Context.rename");
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		// TODO implement Context.rename
		throw new UnsupportedOperationException("TODO implement Context.rename");
	}

	@Override
	public void unbind(Name name) throws NamingException {
		// TODO implement Context.unbind
		throw new UnsupportedOperationException("TODO implement Context.unbind");
	}

	@Override
	public void unbind(String name) throws NamingException {
		// TODO implement Context.unbind
		throw new UnsupportedOperationException("TODO implement Context.unbind");
	}

}