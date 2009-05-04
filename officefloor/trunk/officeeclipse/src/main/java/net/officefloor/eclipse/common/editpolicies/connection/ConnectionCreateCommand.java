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
package net.officefloor.eclipse.common.editpolicies.connection;

import net.officefloor.model.ConnectionModel;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * Provides generic {@link org.eclipse.gef.commands.Command} to create a
 * connection.
 * 
 * @author Daniel
 */
public class ConnectionCreateCommand extends Command {

	/**
	 * {@link ConnectionGraphicalNodeEditPolicy} that created this command.
	 */
	protected final ConnectionGraphicalNodeEditPolicy policy;

	/**
	 * Source of the connection.
	 */
	protected final Object source;

	/**
	 * Target of the connection.
	 */
	protected Object target = null;

	/**
	 * {@link CreateConnectionRequest}.
	 */
	protected CreateConnectionRequest createConnectionRequest = null;

	/**
	 * Connection that was created.
	 */
	protected ConnectionModel connection = null;

	/**
	 * Initiate with source of connection.
	 * 
	 * @param source
	 *            Source of connection.
	 */
	public ConnectionCreateCommand(ConnectionGraphicalNodeEditPolicy policy,
			Object source) {
		this.policy = policy;
		this.source = source;
	}

	/**
	 * Indicates if connection may target the input model.
	 * 
	 * @param model
	 *            Model to possibly be the target.
	 * @return True if may target the model.
	 */
	public boolean canTarget(Object model) {
		// Indicate if may target
		return this.policy.canTarget(model);
	}

	/**
	 * Specifies the target for this connection.
	 * 
	 * @param model
	 *            Model to be target of connection.
	 */
	public void setTarget(Object model) {
		this.target = model;
	}

	/**
	 * Specifies the {@link CreateConnectionRequest}.
	 * 
	 * @param request
	 *            {@link CreateConnectionRequest}.
	 */
	public void setCreateConnectionRequest(CreateConnectionRequest request) {
		this.createConnectionRequest = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// Create the connection
		this.connection = policy.createConnection(this.source, this.target,
				this.createConnectionRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		// Remove the connection
		this.connection.remove();
	}

}