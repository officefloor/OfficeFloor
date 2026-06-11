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

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockSectionSource extends AbstractSectionSource {

    private static final Set<String> invoked = new HashSet<>();

    public static void clear() {
        invoked.clear();
    }

    public static boolean isInvoked(String functionName) {
        return invoked.contains(functionName);
    }

    /*
     * =================== SectionSource =====================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
        assertEquals("mock", context.getSectionLocation());

        // Load the functions
        String functionNames = context.getProperty("functions");
        for (String functionName : functionNames.split(",")) {

            // Add the function
            SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName, MockManagedFunctionSource.class.getName());
            namespace.addProperty(MockManagedFunctionSource.PROPERTY_FUNCTION_NAME, functionName);
            SectionFunction function = namespace.addSectionFunction(functionName, functionName);

            // Link input
            designer.link(designer.addSectionInput(functionName, null), function);
        }
    }

    public static class MockManagedFunctionSource extends AbstractManagedFunctionSource {

        public static final String PROPERTY_FUNCTION_NAME = "function";

        private String functionName;

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty(PROPERTY_FUNCTION_NAME);
        }

        @Override
        public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder, ManagedFunctionSourceContext context) throws Exception {
            String functionName = context.getProperty(PROPERTY_FUNCTION_NAME);
            ManagedFunctionTypeBuilder<None, None> functionBuilder = functionNamespaceTypeBuilder.addManagedFunctionType(functionName, None.class, None.class);
            functionBuilder.setFunctionFactory(() -> (managedFunctionContext) -> {
                MockSectionSource.invoked.add(functionName);
            });
        }
    }

}
