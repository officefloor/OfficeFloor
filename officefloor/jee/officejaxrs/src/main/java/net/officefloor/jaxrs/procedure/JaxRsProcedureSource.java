package net.officefloor.jaxrs.procedure;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * JAX-RS {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * Source name for {@link JaxRsProcedureSource}.
	 */
	public static final String SOURCE_NAME = "JAXRS";

	/*
	 * ================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= ManagedFunctionProcedureSource ==================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// TODO Implement ProcedureSource.listProcedures
		throw new IllegalStateException("TODO implement ProcedureSource.listProcedures");
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		
		
		
		// TODO Implement ManagedFunctionProcedureSource.loadManagedFunction
		throw new IllegalStateException("TODO implement ManagedFunctionProcedureSource.loadManagedFunction");
	}

}