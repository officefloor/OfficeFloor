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
package net.officefloor.eclipse.common.editparts;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.editpolicies.ConnectionGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;

/**
 * Abstract
 * {@link net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart}
 * that provides functionality to allow this to be a source.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorSourceNodeEditPart<M extends Model, F extends OfficeFloorFigure>
		extends AbstractOfficeFloorNodeEditPart<M, F> {

	/**
	 * Creates the {@link GraphicalNodeEditPolicy} to be used.
	 * 
	 * @return {@link GraphicalNodeEditPolicy}.
	 */
	protected GraphicalNodeEditPolicy createGraphicalNodeEditPolicy() {
		// Obtain the connection factory
		ConnectionModelFactory connectionFactory = this
				.createConnectionModelFactory();

		// Create the list of target types
		List<Class<?>> targetTypes = new LinkedList<Class<?>>();
		this.populateConnectionTargetTypes(targetTypes);

		// Return the graphical node edit policy
		return new ConnectionGraphicalNodeEditPolicy(connectionFactory,
				targetTypes);
	}

	/**
	 * Creates the {@link ConnectionModelFactory} to create connections. If this
	 * should never to a source of a connection return <code>null</code>.
	 * 
	 * @return {@link ConnectionModelFactory} or <code>null</code>.
	 */
	protected abstract ConnectionModelFactory createConnectionModelFactory();

	/**
	 * Populates the types that this may target a connection with.
	 * 
	 * @param types
	 *            List of types to be populated.
	 */
	protected abstract void populateConnectionTargetTypes(List<Class<?>> types);

}
