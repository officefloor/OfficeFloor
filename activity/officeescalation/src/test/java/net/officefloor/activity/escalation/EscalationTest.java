/*-
 * #%L
 * Escalation
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

package net.officefloor.activity.escalation;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.escalation.build.EscalationArchitect;
import net.officefloor.activity.escalation.build.EscalationEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EscalationTest {

    @Test
    public void single() throws Throwable {
        this.doTest((escalationArchitect, properties) ->
                escalationArchitect.addEscalation(IOException.class.getName(),
                        "officefloor/escalation/java.io.IOException.yml", properties),
                ThrowingService.class);
        assertNotNull(HandlerService.exception, "Should handle exception");
        assertInstanceOf(IOException.class, HandlerService.exception, "Incorrect exception type");
        assertEquals("TEST", HandlerService.exception.getMessage(), "Incorrect exception message");
    }

    @Test
    public void directory() throws Throwable {
        this.doTest((escalationArchitect, properties) ->
                escalationArchitect.addEscalations("officefloor/escalation", properties),
                ThrowingService.class);
        assertNotNull(HandlerService.exception, "Should handle exception from directory");
        assertInstanceOf(IOException.class, HandlerService.exception, "Incorrect exception type");
        assertEquals("TEST", HandlerService.exception.getMessage(), "Incorrect exception message");
    }

    @Test
    public void singleUnchecked() throws Throwable {
        this.doTest((escalationArchitect, properties) ->
                escalationArchitect.addEscalation(IllegalStateException.class.getName(),
                        "officefloor/escalation/java.lang.IllegalStateException.yml", properties),
                UncheckedThrowingService.class);
        assertNotNull(UncheckedHandlerService.exception, "Should handle unchecked exception");
        assertInstanceOf(IllegalStateException.class, UncheckedHandlerService.exception, "Incorrect exception type");
        assertEquals("UNCHECKED", UncheckedHandlerService.exception.getMessage(), "Incorrect exception message");
    }

    @Test
    public void directoryUnchecked() throws Throwable {
        this.doTest((escalationArchitect, properties) ->
                escalationArchitect.addEscalations("officefloor/escalation", properties),
                UncheckedThrowingService.class);
        assertNotNull(UncheckedHandlerService.exception, "Should handle unchecked exception from directory");
        assertInstanceOf(IllegalStateException.class, UncheckedHandlerService.exception, "Incorrect exception type");
        assertEquals("UNCHECKED", UncheckedHandlerService.exception.getMessage(), "Incorrect exception message");
    }

    @FunctionalInterface
    protected interface SetupEscalation {
        void setup(EscalationArchitect escalationArchitect, PropertyList properties) throws Exception;
    }

    private void doTest(SetupEscalation setup, Class<?> serviceClass) throws Throwable {
        HandlerService.exception = null;
        UncheckedHandlerService.exception = null;
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((office) -> {
            OfficeArchitect officeArchitect = office.getOfficeArchitect();
            OfficeSourceContext sourceContext = office.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, sourceContext);
            EscalationArchitect escalationArchitect = EscalationEmployer.employEscalationArchitect(officeArchitect, composeArchitect, sourceContext);

            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());

            setup.setup(escalationArchitect, properties);

            office.addSection("service", serviceClass);
        });

        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            CompileOfficeFloor.invokeProcess(officeFloor, "service.service", null);
        }
    }

    public static class ThrowingService {
        public void service() throws IOException {
            throw new IOException("TEST");
        }
    }

    public static class HandlerService {
        public static IOException exception = null;

        public void handle(@Parameter IOException ex) {
            HandlerService.exception = ex;
        }
    }

    public static class UncheckedThrowingService {
        public void service() {
            throw new IllegalStateException("UNCHECKED");
        }
    }

    public static class UncheckedHandlerService {
        public static IllegalStateException exception = null;

        public void handle(@Parameter IllegalStateException ex) {
            UncheckedHandlerService.exception = ex;
        }
    }

}
