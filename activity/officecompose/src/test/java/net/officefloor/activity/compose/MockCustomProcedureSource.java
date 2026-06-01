/*-
 * #%L
 * Composition
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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
