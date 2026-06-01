package net.officefloor.activity.compose;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.ServiceContext;

public class MockCustomProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

    public static final String SOURCE_NAME = "MockCustom";

    public static boolean isRun = false;

    /*
     * ================ ProcedureSourceServiceFactory =================
     */

    @Override
    public ProcedureSource createService(ServiceContext context) throws Throwable {
        return this;
    }

    /*
     * ==================== ProcedureSource ===========================
     */

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public void listProcedures(ProcedureListContext context) throws Exception {
        if ("mock-resource".equals(context.getResource())) {
            context.addProcedure("run");
        }
    }

    @Override
    public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
        context.setManagedFunction(() -> (managedFunctionContext) -> {
            isRun = true;
        }, None.class, None.class);
    }

}
