/*-
 * #%L
 * Scala
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
