package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;

/**
 * {@link OfficeFloorCompilerRunnable} to create {@link ProcedureLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderCompilerRunnable implements OfficeFloorCompilerRunnable<ProcedureLoader> {

	/*
	 * ===================== OfficeFloorCompilerRunnable ====================
	 */

	@Override
	public ProcedureLoader run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {
		return new ProcedureLoaderImpl(compiler);
	}

}