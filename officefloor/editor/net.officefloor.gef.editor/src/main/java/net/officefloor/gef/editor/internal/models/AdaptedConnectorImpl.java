package net.officefloor.gef.editor.internal.models;

import java.util.List;

import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted connector.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectorImpl<R extends Model, O, M extends Model> implements AdaptedConnector<M> {

	/**
	 * Parent {@link AdaptedConnectable} containing this
	 * {@link AdaptedConnectorImpl}.
	 */
	private final AdaptedConnectable<M> parentAdaptedConnectable;

	/**
	 * {@link ConnectionModel} {@link Class}.
	 */
	private final Class<? extends ConnectionModel> connectionClass;

	/**
	 * {@link AdaptedConnectorRole}. May be <code>null</code> to fulfill all roles.
	 */
	private final AdaptedConnectorRole role;

	/**
	 * {@link ModelToConnection} for this {@link AdaptedConnectorImpl}.
	 */
	private final ModelToConnection<R, O, M, ?, ? extends ConnectionModel> modelToConnection;

	/**
	 * Associated {@link AdaptedConnector} instances.
	 */
	private List<AdaptedConnector<M>> associatedAdaptedConnectors;

	/**
	 * Associated {@link AdaptedConnectorRole}.
	 */
	private AdaptedConnectorRole associatedRole;

	/**
	 * Instantiate.
	 * 
	 * @param parentAdaptedConnectable Parent {@link AdaptedConnectable} containing
	 *                                 this {@link AdaptedConnectorImpl}.
	 * @param connectionClass          {@link ConnectionModel} {@link Class}.
	 * @param role                     {@link AdaptedConnectorRole}.
	 * @param modelToConnection        {@link ModelToConnection} for the
	 *                                 {@link ConnectionModel} {@link Class}.
	 */
	public AdaptedConnectorImpl(AdaptedConnectable<M> parentAdaptedConnectable,
			Class<? extends ConnectionModel> connectionClass, AdaptedConnectorRole role,
			ModelToConnection<R, O, M, ?, ? extends ConnectionModel> modelToConnection) {
		this.parentAdaptedConnectable = parentAdaptedConnectable;
		this.connectionClass = connectionClass;
		this.role = role;
		this.modelToConnection = modelToConnection;
	}

	/*
	 * ================= AdaptedConnector ==================
	 */

	@Override
	public AdaptedConnectable<M> getParentAdaptedConnectable() {
		return this.parentAdaptedConnectable;
	}

	@Override
	public Class<? extends ConnectionModel> getConnectionModelClass() {
		return this.connectionClass;
	}

	@Override
	public void setAssociation(List<AdaptedConnector<M>> associatedAdaptedConnectors,
			AdaptedConnectorRole associatedRole) {
		this.associatedAdaptedConnectors = associatedAdaptedConnectors;
		this.associatedRole = associatedRole;

		// Ensure associated role is same
		if ((this.role != null) && (!(this.role.equals(associatedRole)))) {
			throw new IllegalStateException(
					"Associated role does not match role of connector for " + this.connectionClass.getName()
							+ " of model" + this.parentAdaptedConnectable.getModel().getClass().getName());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isAssociationCreateConnection() {
		for (AdaptedConnector<M> connector : this.associatedAdaptedConnectors) {
			AdaptedConnectorImpl<R, O, M> impl = (AdaptedConnectorImpl<R, O, M>) connector;

			// Determine if can create connection
			if (impl.modelToConnection.getAdaptedConnectionFactory().canCreateConnection()) {
				return true; // able to create connection
			}
		}

		// As here, not able to create connection
		return false;
	}

	@Override
	public AdaptedConnectorRole getAssociationRole() {
		return this.associatedRole;
	}

}