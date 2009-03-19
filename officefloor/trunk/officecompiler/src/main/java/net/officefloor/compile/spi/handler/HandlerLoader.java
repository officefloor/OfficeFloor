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
package net.officefloor.compile.spi.handler;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.handler.source.HandlerSourceProperty;
import net.officefloor.compile.spi.handler.source.HandlerSource;
import net.officefloor.compile.spi.handler.source.HandlerSpecification;

/**
 * Loads the {@link HandlerType} from the {@link HandlerSource}.
 * 
 * @author Daniel
 */
public interface HandlerLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link HandlerSpecification} for the {@link HandlerSource}.
	 * 
	 * @param handlerSourceClass
	 *            Class of the {@link HandlerSource}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link HandlerSpecification} and obtaining the
	 *            {@link PropertyList}.
	 * @return {@link PropertyList} of the {@link HandlerSourceProperty} instances of
	 *         the {@link HandlerSpecification} or <code>null</code> if issue,
	 *         which are reported to the {@link CompilerIssues}.
	 */
	<HS extends HandlerSource> PropertyList loadSpecification(
			Class<HS> handlerSourceClass, CompilerIssues issues);

	/**
	 * Loads and returns the {@link HandlerType} sourced from the
	 * {@link HandlerSource}.
	 * 
	 * @param handlerSourceClass
	 *            Class of the {@link HandlerSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link HandlerType}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link HandlerSource} may use in
	 *            obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link HandlerType}.
	 * @return {@link HandlerType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<F extends Enum<F>, HS extends HandlerSource> HandlerType<F> loadHandler(
			Class<HS> handlerSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues);

}