package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureProperty;
import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * {@link Procedure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureImpl implements Procedure {

	/**
	 * Procedure name.
	 */
	private final String procedureName;

	/**
	 * {@link ProcedureSource} name.
	 */
	private final String serviceName;

	/**
	 * {@link ProcedureProperty} instances.
	 */
	private final ProcedureProperty[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param procedureName Procedure name.
	 * @param serviceName   {@link ProcedureSource} name.
	 * @param properties    {@link ProcedureProperty} instances.
	 */
	public ProcedureImpl(String procedureName, String serviceName, ProcedureProperty[] properties) {
		this.procedureName = procedureName;
		this.serviceName = serviceName;
		this.properties = properties;
	}

	/*
	 * =================== Procedure ======================
	 */

	@Override
	public String getProcedureName() {
		return this.procedureName;
	}

	@Override
	public String getServiceName() {
		return this.serviceName;
	}

	@Override
	public ProcedureProperty[] getProperties() {
		return this.properties;
	}

}