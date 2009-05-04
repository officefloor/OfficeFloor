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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.editpolicies.ConnectionGraphicalNodeEditPolicy;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;

/**
 * Abstract {@link EditPart} for a node of the Office.
 * 
 * @author Daniel
 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart
 */
public abstract class AbstractOfficeFloorNodeEditPart<M extends Model, F extends OfficeFloorFigure>
		extends AbstractOfficeFloorEditPart<M, F> implements NodeEditPart {

	/*
	 * =================== AbstractEditPart =============================
	 */
	
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();

		// Install the graphical node edit policy
		this.installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, this
				.createGraphicalNodeEditPolicy());
	}

	/**
	 * Creates the {@link GraphicalNodeEditPolicy} to be used.
	 * 
	 * @return {@link GraphicalNodeEditPolicy}.
	 */
	@SuppressWarnings( { "unchecked" })
	protected GraphicalNodeEditPolicy createGraphicalNodeEditPolicy() {
		return new ConnectionGraphicalNodeEditPolicy(null,
				(List<Class<?>>) Collections.EMPTY_LIST);
	}

	@Override
	protected List<?> getModelSourceConnections() {
		// Create list of connections
		List<Object> connections = new LinkedList<Object>();

		// Populate the Source Connection Models
		this.populateConnectionSourceModels(connections);

		// Return the source connection models
		return connections;
	}

	/**
	 * Populates the Models that are sources of connections.
	 * 
	 * @param models
	 *            List to be populated with Models that are the sources of
	 *            connections.
	 */
	protected abstract void populateConnectionSourceModels(List<Object> models);

	@Override
	protected List<?> getModelTargetConnections() {
		// Create list of connections
		List<Object> connections = new LinkedList<Object>();

		// Populate the Target Connection Models
		this.populateConnectionTargetModels(connections);

		// Return the source connection models
		return connections;
	}

	/**
	 * Populates the Models that are targets of connections.
	 * 
	 * @param models
	 *            List to be populated with Models that are the targets of
	 *            connections.
	 */
	protected abstract void populateConnectionTargetModels(List<Object> models);

	@Override
	@SuppressWarnings("unchecked")
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		// Obtain the type of connection
		Class connectionModelType = connection.getModel().getClass();
		ConnectionAnchor anchor = this.getOfficeFloorFigure()
				.getSourceConnectionAnchor(connectionModelType);
		if (anchor != null) {
			return anchor;
		}

		// No anchor so provide around figure
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	@SuppressWarnings("unchecked")
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		// Obtain the type of connection
		Class connectionModelType = connection.getModel().getClass();
		ConnectionAnchor anchor = this.getOfficeFloorFigure()
				.getTargetConnectionAnchor(connectionModelType);
		if (anchor != null) {
			return anchor;
		}

		// No anchor so provide around figure
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}
}