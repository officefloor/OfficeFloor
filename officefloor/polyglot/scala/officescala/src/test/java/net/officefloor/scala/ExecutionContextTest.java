package net.officefloor.scala;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import scala.concurrent.ExecutionContext;

/**
 * Ensure can provide {@link ExecutionContext} as parameter.
 */
public class ExecutionContextTest extends OfficeFrameTestCase {

    /**
     * Ensure can provide {@link ExecutionContext} as parameter.
     */
    public void testExecutionContext() throws Throwable {
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office(context -> {
            ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer.employProcedureArchitect(context.getOfficeArchitect(), context.getOfficeSourceContext());
            procedureArchitect.addProcedure("procedure", ExecutionContextProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME, "procedure", false, null);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            ExecutionContextProcedure.reset();
            CompileOfficeFloor.invokeProcess(officeFloor, "procedure.procedure", null);
            assertNotNull("Should have " + ExecutionContext.class.getName(), ExecutionContextProcedure.executionContext());
        }
    }

}
