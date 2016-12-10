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
package net.officefloor.eclipse.common.editpolicies.connection;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * {@link EditPolicy} for creating connections.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

	/**
	 * {@link Link} instances.
	 */
	private List<Link> links = new LinkedList<Link>();

	/**
	 * Registers a {@link ConnectionChangeFactory}.
	 * 
	 * @param <S>
	 *            Source type.
	 * @param <T>
	 *            Target type.
	 * @param sourceType
	 *            Source {@link Model} type.
	 * @param targetType
	 *            Target {@link Model} type.
	 * @param factory
	 *            {@link ConnectionChangeFactory}.
	 */
	public <S, T> void addConnection(Class<S> sourceType, Class<T> targetType,
			ConnectionChangeFactory<S, T> factory) {
		this.links.add(new Link(sourceType, targetType, factory));
	}

	/*
	 * =============== GraphicalNodeEditPolicy =================================
	 */

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {

		// Obtain the model
		Object model = this.getHost().getModel();

		// Determine if model can be source of connection
		Class<?> modelType = model.getClass();
		boolean isSource = false;
		for (Link link : this.links) {
			if (link.sourceType.equals(modelType)) {
				isSource = true;
				break;
			}
		}
		if (!isSource) {
			return null; // model can not be source
		}

		// Create the command and make available to request
		ConnectionCreateCommand command = new ConnectionCreateCommand(model);
		request.setStartCommand(command);
		return command;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Command getConnectionCompleteCommand(
			final CreateConnectionRequest request) {

		// Obtain create command
		Command startCommand = request.getStartCommand();
		if (!(startCommand instanceof ConnectionCreateCommand)) {
			return null;
		}
		ConnectionCreateCommand createCommand = (ConnectionCreateCommand) startCommand;

		// Obtain the source type
		final Object source = createCommand.getSource();
		Class<?> sourceType = source.getClass();

		// Obtain the target type
		final Object target = this.getHost().getModel();
		Class<?> targetType = target.getClass();

		// Obtain link to connect source to target
		Link connectLink = null;
		FOUND: for (Link link : this.links) {
			if (link.sourceType.equals(sourceType)) {
				if (link.targetType.equals(targetType)) {
					connectLink = link;
					break FOUND;
				}
			}
		}
		if (connectLink == null) {
			return null; // no link to connect source to target
		}

		// Wrap creating change in command to only occur on connection creation
		final ConnectionChangeFactory factory = connectLink.factory;
		return new OfficeFloorCommand() {

			/**
			 * Flag indicated if attempted to create {@link Change}.
			 */
			private boolean isChangeCreated = false;

			/**
			 * {@link Change}.
			 */
			private Change<?> change = null;

			@Override
			protected void doCommand() {
				// Lazy load the change
				if (!this.isChangeCreated) {
					this.isChangeCreated = true;
					this.change = factory.createChange(source, target, request);
				}

				// Apply change if have change
				if (this.change != null) {
					this.change.apply();
				}
			}

			@Override
			protected void undoCommand() {
				// Revert if have change
				if (this.change != null) {
					this.change.revert();
				}
			}
		};
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Link for {@link ConnectionModel}.
	 */
	private class Link {

		/**
		 * Source type.
		 */
		public final Class<?> sourceType;

		/**
		 * Target type.
		 */
		public final Class<?> targetType;

		/**
		 * {@link ConnectionChangeFactory}.
		 */
		@SuppressWarnings("rawtypes")
		public final ConnectionChangeFactory factory;

		/**
		 * Initiate.
		 * 
		 * @param sourceType
		 *            Source type.
		 * @param targetType
		 *            Target type.
		 * @param factory
		 *            {@link ConnectionChangeFactory}.
		 */
		public Link(Class<?> sourceType, Class<?> targetType,
				ConnectionChangeFactory<?, ?> factory) {
			this.sourceType = sourceType;
			this.targetType = targetType;
			this.factory = factory;
		}
	}

}