package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ComposeArchitect}.
 */
public class ComposeArchitectTest {

    @Test
    public void singleMethod() throws Throwable {
        SingleMethod.isRun = false;
        this.doTest("singleMethod.yaml", null);
        assertTrue(SingleMethod.isRun, "Should run function");
    }

    public static class SingleMethod {
        private static boolean isRun = false;
        public void procedure() {
            isRun = true;
        }
    }

    @Test
    public void multipleMethods() throws Throwable {
        MultipleMethods.isRun = false;
        this.doTest("multipleMethods.yaml", null);
        assertTrue(MultipleMethods.isRun, "Should run function");
    }

    public static class MultipleMethods {
        private static boolean isRun = false;
        public void methodOne() {
            isRun = true;
        }
        public void methodTwo() {
        }
    }

    @Test
    public void parameter() throws Throwable {
        final String PARAMETER = "TEST";
        ParameterProcedure.parameter = null;
        this.doTest("parameter.yaml", PARAMETER);
        assertSame(PARAMETER, ParameterProcedure.parameter, "Should pass in parameter");
    }

    public static class ParameterProcedure {
        private static String parameter = null;
        public void procedure(@Parameter String parameter) {
            ParameterProcedure.parameter = parameter;
        }
    }

    @Test
    public void next() throws Throwable {
        SingleMethod.isRun = false;
        this.doTest("next.yaml", null);
        assertTrue(SingleMethod.isRun, "Should run function");
    }

    public static class NextProcedure {
        public void procedure() {
        }
    }

    @Test
    public void nextWithParameter() throws Throwable {
        final String PARAMETER = "PASS";
        ParameterProcedure.parameter = null;
        this.doTest("nextWithParameter.yaml", PARAMETER);
        assertSame(PARAMETER, ParameterProcedure.parameter, "Should pass parameter through");
    }

    public static class PassParameterProcedure {
        public String procedure(@Parameter String parameter) {
            return parameter;
        }
    }

    @Test
    public void flowInvoked() throws Throwable {
        SingleMethod.isRun = false;
        this.doTest("flow.yaml", "invoke");
        assertTrue(SingleMethod.isRun, "Should trigger flow to run function");
    }

    @Test
    public void flowNotInvoked() throws Throwable {
        SingleMethod.isRun = false;
        this.doTest("flow.yaml", null);
        assertFalse(SingleMethod.isRun, "Should not trigger flow");
    }

    public static class FlowProcedure {
        public void procedure(@Parameter String isRun, @Flow("flow") Runnable flow) {
            if (isRun != null) {
                flow.run();
            }
        }
    }

    @Test
    public void escalationThrown() throws Throwable {
        try {
            this.doTest("escalation.yaml", "throw");
            fail("Should not be successful");
        } catch (IOException ex) {
            assertEquals("TEST", ex.getMessage(), "Incorrect escalation");
        }
    }

    @Test
    public void escalationNotThrown() throws Throwable {
        this.doTest("escalation.yaml", null);
    }

    public static class EscalationProcedure {
        public void procedure(@Parameter String isThrow) throws IOException {
            if (isThrow != null) {
                throw new IOException("TEST");
            }
        }
    }

    @Test
    public void escalationHandledByFunction() throws Throwable {
        this.doTest("escalationHandledByFunction.yaml", "throw");
    }

    @Test
    public void escalationHandledByComposition() throws Throwable {
        this.doTest("escalationHandledByComposition.yaml", "throw");
    }

    public static class EscalationHandler {
        public static IOException exception = null;
        public void handle(@Parameter IOException exception) {
            EscalationHandler.exception = exception;
        }
    }

    @Test
    public void managedObject() throws Throwable {
        ManagedObjectProcedure.managedObject = null;
        this.doTest("managedObject.yaml", null);
        assertNotNull(ManagedObjectProcedure.managedObject, "Should inject managed object");
    }

    public static class MockManagedObject {
    }

    public static class ManagedObjectProcedure {
        public static MockManagedObject managedObject = null;
        public void procedure(MockManagedObject managedObject) {
            ManagedObjectProcedure.managedObject = managedObject;
        }
    }

    @Test @Disabled
    public void sectionSource() throws Throwable {
        this.doTest("sectionSource.yaml", null);
    }

    /**
     * Undertakes the composition test.
     *
     * @param configurationFile Configuration of composition.
     * @param parameter Parameter to invoke the composition.
     */
    private void doTest(String configurationFile, Object parameter) throws Throwable {

        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Add the composition
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            OfficeSection section = composeArchitect.addComposition("compose", "architect/" + configurationFile, properties);

            // Allow invoking the composition
            OfficeSection trigger = office.addSection("trigger", Trigger.class);
            office.getOfficeArchitect().link(trigger.getOfficeSectionOutput("handle"), section.getOfficeSectionInput(ComposeArchitect.INPUT_NAME));

            // Provide a managed object
            office.addManagedObject("mo", MockManagedObject.class, ManagedObjectScope.THREAD);
        });

        // Test
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

            // Invoke the composition
            CompileOfficeFloor.invokeProcess(officeFloor, "trigger.trigger", parameter);

        }
    }

    public static class Trigger {
        @Next("handle")
        public String trigger(@Parameter String parameter) {
            return parameter;
        }
    }

}
