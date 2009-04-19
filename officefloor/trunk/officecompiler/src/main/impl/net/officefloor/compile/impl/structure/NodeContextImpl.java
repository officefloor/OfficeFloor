/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link NodeContext} implementation.
 * 
 * @author Daniel
 */
public class NodeContextImpl implements NodeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate default to use {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public NodeContextImpl(ClassLoader classLoader, CompilerIssues issues) {
		this(new ClassLoaderConfigurationContext(classLoader), classLoader,
				issues);
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public NodeContextImpl(ConfigurationContext configurationContext,
			ClassLoader classLoader, CompilerIssues issues) {
		this.configurationContext = configurationContext;
		this.classLoader = classLoader;
		this.issues = issues;
	}

	/*
	 * ================== NodeContext ======================================
	 */

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public CompilerIssues getCompilerIssues() {
		return this.issues;
	}

}