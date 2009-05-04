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

import java.util.List;

import net.officefloor.model.ConnectionModel;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * {@link EditPolicy} for creating connections.
 * 
 * @author Daniel
 */
public class ConnectionGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

	/**
	 * Creates the connection.
	 */
	private final ConnectionModelFactory connectionFactory;

	/**
	 * List of target types allowable.
	 */
	private final List<Class<?>> targetTypes;

	/**
	 * Indicates details of how the {@link EditPolicy} may participate in
	 * connections.
	 * 
	 * @param connectionFactory
	 *            Specific functionality to create a connection the
	 *            {@link EditPart}. Inputing <code>null</code> indicates the
	 *            {@link EditPart} can not be a source of a connection.
	 * @param targetTypes
	 *            List of types which may be a target of a connection.
	 */
	public ConnectionGraphicalNodeEditPolicy(
			ConnectionModelFactory connectionFactory, List<Class<?>> targetTypes) {
		this.connectionFactory = connectionFactory;
		this.targetTypes = targetTypes;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {

		// Check if able to source connections
		if (this.connectionFactory == null) {
			// Not able to source
			return null;
		}

		// Obtain the model
		Object model = this.getHost().getModel();

		// Create the Connection
		ConnectionCreateCommand command = new ConnectionCreateCommand(this,
				model);

		// Specify the create command
		request.setStartCommand(command);

		// Return command
		return command;
	}

	@Override
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {

		// Check if appropriate create command
		if (!(request.getStartCommand() instanceof ConnectionCreateCommand)) {
			return null;
		}

		// Obtain command
		ConnectionCreateCommand command = (ConnectionCreateCommand) request
				.getStartCommand();

		// Obtain the host's model
		Object model = this.getHost().getModel();

		// Check if can target the host
		if (!command.canTarget(model)) {
			// Not able to target
			return null;
		}

		// Specify host model as target
		command.setTarget(model);

		// Specify the create connection request
		command.setCreateConnectionRequest(request);

		// Return command
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Indicates if the model may be the target of the created connection.
	 * 
	 * @param model
	 *            Model to be target checked if may be target of created
	 *            connection.
	 * @return True if model may be target of created connection.
	 */
	public boolean canTarget(Object model) {

		// Check if may target
		for (Class<?> type : this.targetTypes) {
			if (type.isAssignableFrom(model.getClass())) {
				// Able to assign thus may target
				return true;
			}
		}

		// May not assign thus may not target
		return false;
	}

	/**
	 * Creates the connection.
	 * 
	 * @param source
	 *            Source model of connection.
	 * @param target
	 *            Target model of connection.
	 * @param request
	 *            {@link CreateConnectionRequest}.
	 * @return Connection.
	 */
	public ConnectionModel createConnection(Object source, Object target,
			CreateConnectionRequest request) {
		return this.connectionFactory.createConnection(source, target, request);
	}

}