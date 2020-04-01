package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link ProcedureSource} to obtain type indication.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeProcedureSource implements ProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * Indicates if type.
	 */
	public static Boolean isType = null;

	/**
	 * Source name.
	 */
	public static final String SOURCE_NAME = "Type";

	/**
	 * {@link Method} for {@link Procedure}.
	 */
	public void method() {
		// Should not be invoked
	}

	/*
	 * ==================== ProcedureSourceServiceFactory ==========================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================= ProcedureSource ===================================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// Should not be for listing
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {

		// Indicate if type
		isType = context.getSourceContext().isLoadingType();

		// Return method
		Class<?> clazz = context.getSourceContext().loadClass(context.getResource());
		return clazz.getMethod(context.getProcedureName());
	}

}