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
package net.officefloor.plugin.jndi.dircontext;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Synchronised {@link DirContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class SynchronisedDirContext implements DirContext {

	/**
	 * Delegate {@link DirContext}.
	 */
	private final DirContext delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link DirContext}.
	 */
	public SynchronisedDirContext(DirContext delegate) {
		this.delegate = delegate;
	}

	/*
	 * ======================= DirContext =========================
	 */

	@Override
	public synchronized void bind(Name name, Object obj, Attributes attrs)
			throws NamingException {
		this.delegate.bind(name, obj, attrs);
	}

	@Override
	public synchronized void bind(String name, Object obj, Attributes attrs)
			throws NamingException {
		this.delegate.bind(name, obj, attrs);
	}

	@Override
	public synchronized DirContext createSubcontext(Name name, Attributes attrs)
			throws NamingException {
		return this.delegate.createSubcontext(name, attrs);
	}

	@Override
	public synchronized DirContext createSubcontext(String name,
			Attributes attrs) throws NamingException {
		return this.delegate.createSubcontext(name, attrs);
	}

	@Override
	public synchronized Attributes getAttributes(Name name)
			throws NamingException {
		return this.delegate.getAttributes(name);
	}

	@Override
	public synchronized Attributes getAttributes(String name)
			throws NamingException {
		return this.delegate.getAttributes(name);
	}

	@Override
	public synchronized Attributes getAttributes(Name name, String[] attrIds)
			throws NamingException {
		return this.delegate.getAttributes(name, attrIds);
	}

	@Override
	public synchronized Attributes getAttributes(String name, String[] attrIds)
			throws NamingException {
		return this.delegate.getAttributes(name, attrIds);
	}

	@Override
	public synchronized DirContext getSchema(Name name) throws NamingException {
		return this.delegate.getSchema(name);
	}

	@Override
	public synchronized DirContext getSchema(String name)
			throws NamingException {
		return this.delegate.getSchema(name);
	}

	@Override
	public synchronized DirContext getSchemaClassDefinition(Name name)
			throws NamingException {
		return this.delegate.getSchemaClassDefinition(name);
	}

	@Override
	public synchronized DirContext getSchemaClassDefinition(String name)
			throws NamingException {
		return this.delegate.getSchemaClassDefinition(name);
	}

	@Override
	public synchronized void modifyAttributes(Name name, ModificationItem[] mods)
			throws NamingException {
		this.delegate.modifyAttributes(name, mods);
	}

	@Override
	public synchronized void modifyAttributes(String name,
			ModificationItem[] mods) throws NamingException {
		this.delegate.modifyAttributes(name, mods);
	}

	@Override
	public synchronized void modifyAttributes(Name name, int modOp,
			Attributes attrs) throws NamingException {
		this.delegate.modifyAttributes(name, modOp, attrs);
	}

	@Override
	public synchronized void modifyAttributes(String name, int modOp,
			Attributes attrs) throws NamingException {
		this.delegate.modifyAttributes(name, modOp, attrs);
	}

	@Override
	public synchronized void rebind(Name name, Object obj, Attributes attrs)
			throws NamingException {
		this.delegate.rebind(name, obj, attrs);
	}

	@Override
	public synchronized void rebind(String name, Object obj, Attributes attrs)
			throws NamingException {
		this.delegate.rebind(name, obj, attrs);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(Name name,
			Attributes matchingAttributes) throws NamingException {
		return this.delegate.search(name, matchingAttributes);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(String name,
			Attributes matchingAttributes) throws NamingException {
		return this.delegate.search(name, matchingAttributes);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(Name name,
			Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		return this.delegate.search(name, matchingAttributes,
				attributesToReturn);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(String name,
			Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		return this.delegate.search(name, matchingAttributes,
				attributesToReturn);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(Name name,
			String filter, SearchControls cons) throws NamingException {
		return this.delegate.search(name, filter, cons);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(String name,
			String filter, SearchControls cons) throws NamingException {
		return this.delegate.search(name, filter, cons);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(Name name,
			String filterExpr, Object[] filterArgs, SearchControls cons)
			throws NamingException {
		return this.delegate.search(name, filterExpr, filterArgs, cons);
	}

	@Override
	public synchronized NamingEnumeration<SearchResult> search(String name,
			String filterExpr, Object[] filterArgs, SearchControls cons)
			throws NamingException {
		return this.delegate.search(name, filterExpr, filterArgs, cons);
	}

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