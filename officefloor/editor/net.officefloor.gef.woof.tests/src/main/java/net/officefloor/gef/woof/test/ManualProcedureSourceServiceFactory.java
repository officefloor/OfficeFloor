package net.officefloor.gef.woof.test;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Manual {@link ProcedureSourceServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManualProcedureSourceServiceFactory implements ProcedureSourceServiceFactory, ProcedureSource {

	public static void procedure() {
	}

	/*
	 * ===================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ProcedureSource ================================
	 */

	@Override
	public String getSourceName() {
		return "Manual";
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		if (MockSection.class.getName().equals(context.getResource())) {
			context.addProcedure(null);
		}
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {
		return this.getClass().getMethod(context.getProcedureName());
	}

}