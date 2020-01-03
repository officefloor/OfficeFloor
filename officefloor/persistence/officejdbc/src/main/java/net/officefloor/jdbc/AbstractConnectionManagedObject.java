package net.officefloor.jdbc;

import java.sql.Connection;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Connection} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionManagedObject implements ManagedObject {

	/**
	 * {@link Connection}.
	 */
	protected Connection connection = null;

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 * @throws Throwable If fails to obtain the {@link Connection}.
	 */
	protected abstract Connection getConnection() throws Throwable;

	/*
	 * ================= ManagedObject =====================
	 */

	@Override
	public Object getObject() throws Throwable {
		if (this.connection == null) {
			this.connection = this.getConnection();
		}
		return this.connection;
	}

}
