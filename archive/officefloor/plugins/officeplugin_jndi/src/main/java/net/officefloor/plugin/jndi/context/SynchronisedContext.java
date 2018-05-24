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
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * <p>
 * Synchronised {@link Context} wrapper of a delegate {@link Context}.
 * <p>
 * This wrapper is necessary as the {@link Context} may be bound to
 * {@link ProcessState} and {@link ThreadState} and be invoked by different
 * {@link Thread} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SynchronisedContext implements Context {

	/**
	 * Delegate {@link Context}.
	 */
	private final Context delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link Context}.
	 */
	public SynchronisedContext(Context delegate) {
		this.delegate = delegate;
	}

	/**
	 * Obtains the delegate {@link Context}.
	 * 
	 * @return Delegate {@link Context}.
	 */
	public Context getDelegateContext() {
		return this.delegate;
	}

	/*
	 * ====================== Context ============================
	 */

	@Override
	public synchronized Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		return this.delegate.addToEnvironment(propName, propVal);
	}

	@Override
	public synchronized void bind(Name name, Object obj) throws NamingException {
		this.delegate.bind(name, obj);
	}

	@Override
	public synchronized void bind(String name, Object obj)
			throws NamingException {
		this.delegate.bind(name, obj);
	}

	@Override
	public synchronized void close() throws NamingException {
		this.delegate.close();
	}

	@Override
	public synchronized Name composeName(Name name, Name prefix)
			throws NamingException {
		return this.delegate.composeName(name, prefix);
	}

	@Override
	public synchronized String composeName(String name, String prefix)
			throws NamingException {
		return this.delegate.composeName(name, prefix);
	}

	@Override
	public synchronized Context createSubcontext(Name name)
			throws NamingException {
		return this.delegate.createSubcontext(name);
	}

	@Override
	public synchronized Context createSubcontext(String name)
			throws NamingException {
		return this.delegate.createSubcontext(name);
	}

	@Override
	public synchronized void destroySubcontext(Name name)
			throws NamingException {
		this.delegate.destroySubcontext(name);
	}

	@Override
	public synchronized void destroySubcontext(String name)
			throws NamingException {
		this.delegate.destroySubcontext(name);
	}

	@Override
	public synchronized Hashtable<?, ?> getEnvironment() throws NamingException {
		return this.delegate.getEnvironment();
	}

	@Override
	public synchronized String getNameInNamespace() throws NamingException {
		return this.delegate.getNameInNamespace();
	}

	@Override
	public synchronized NameParser getNameParser(Name name)
			throws NamingException {
		return this.delegate.getNameParser(name);
	}

	@Override
	public synchronized NameParser getNameParser(String name)
			throws NamingException {
		return this.delegate.getNameParser(name);
	}

	@Override
	public synchronized NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		return this.delegate.list(name);
	}

	@Override
	public synchronized NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		return this.delegate.list(name);
	}

	@Override
	public synchronized NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		return this.delegate.listBindings(name);
	}

	@Override
	public synchronized NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		return this.delegate.listBindings(name);
	}

	@Override
	public synchronized Object lookup(Name name) throws NamingException {
		return this.delegate.lookup(name);
	}

	@Override
	public synchronized Object lookup(String name) throws NamingException {
		return this.delegate.lookup(name);
	}

	@Override
	public synchronized Object lookupLink(Name name) throws NamingException {
		return this.delegate.lookupLink(name);
	}

	@Override
	public synchronized Object lookupLink(String name) throws NamingException {
		return this.delegate.lookupLink(name);
	}

	@Override
	public synchronized void rebind(Name name, Object obj)
			throws NamingException {
		this.delegate.rebind(name, obj);
	}

	@Override
	public synchronized void rebind(String name, Object obj)
			throws NamingException {
		this.delegate.rebind(name, obj);
	}

	@Override
	public synchronized Object removeFromEnvironment(String propName)
			throws NamingException {
		return this.delegate.removeFromEnvironment(propName);
	}

	@Override
	public synchronized void rename(Name oldName, Name newName)
			throws NamingException {
		this.delegate.rename(oldName, newName);
	}

	@Override
	public synchronized void rename(String oldName, String newName)
			throws NamingException {
		this.delegate.rename(oldName, newName);
	}

	@Override
	public synchronized void unbind(Name name) throws NamingException {
		this.delegate.unbind(name);
	}

	@Override
	public synchronized void unbind(String name) throws NamingException {
		this.delegate.unbind(name);
	}

}