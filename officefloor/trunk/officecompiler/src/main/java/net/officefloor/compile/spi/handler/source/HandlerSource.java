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
package net.officefloor.compile.spi.handler.source;

import net.officefloor.compile.spi.handler.HandlerType;
import net.officefloor.frame.api.execute.Handler;

/**
 * Sources the {@link HandlerType}.
 * 
 * @author Daniel
 */
public interface HandlerSource {

	/**
	 * <p>
	 * Obtains the {@link HandlerSpecification} for this {@link HandlerSource}.
	 * <p>
	 * This enables the {@link HandlerSourceContext} to be populated with the
	 * necessary details as per this {@link HandlerSpecification} in loading the
	 * {@link HandlerType}.
	 * 
	 * @return {@link HandlerSpecification}.
	 */
	HandlerSpecification getSpecification();

	/**
	 * Sources the {@link HandlerType} by populating it via the input
	 * {@link HandlerTypeBuilder}.
	 * 
	 * @param handlerTypeBuilder
	 *            {@link HandlerTypeBuilder} to be populated with the
	 *            <code>type definition</code> of the {@link Handler}.
	 * @param context
	 *            {@link HandlerSourceContext} to source details to populate the
	 *            {@link HandlerTypeBuilder}.
	 * @throws Exception
	 *             If fails to populate the {@link HandlerTypeBuilder}.
	 */
	void sourceHandler(HandlerTypeBuilder handlerTypeBuilder,
			HandlerSourceContext context) throws Exception;

}