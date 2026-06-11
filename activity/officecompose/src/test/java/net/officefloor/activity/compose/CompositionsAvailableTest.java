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

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests compositions available.
 */
public class CompositionsAvailableTest {

    @Test
    public void matchingFilter() throws Throwable {
        assertTrue(this.doTest("directory", itemName -> itemName.endsWith(".GET")),
                "Should be available when a YAML file matches the filter");
    }

    @Test
    public void nonMatchingFilter() throws Throwable {
        assertFalse(this.doTest("directory", itemName -> itemName.endsWith(".PUT")),
                "Should not be available when no YAML files match the filter");
    }

    @Test
    public void nonExistentDirectory() throws Throwable {
        assertFalse(this.doTest("nonexistent", itemName -> true),
                "Should not be available when the directory does not exist");
    }

    @FunctionalInterface
    interface Check {
        boolean check(ComposeArchitect architect) throws Exception;
    }

    private boolean doTest(String resourceDirectory, java.util.function.Predicate<String> filter) throws Throwable {
        return this.doTest(architect -> architect.isCompositionsAvailable(resourceDirectory, filter));
    }

    private boolean doTest(Check check) throws Throwable {
        Closure<Boolean> result = new Closure<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect architect = ComposeEmployer.employComposeArchitect(
                    office.getOfficeArchitect(), office.getOfficeSourceContext());
            result.value = check.check(architect);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
        }
        return result.value;
    }

}
