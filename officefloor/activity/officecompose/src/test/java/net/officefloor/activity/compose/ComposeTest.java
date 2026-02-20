package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import static net.officefloor.frame.test.OfficeFrameTestCase.fail;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComposeTest {

    @Test
    public void singleMethod() {
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
    public void multipleMethods() {
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
    public void parameter() {
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
    public void next() {
        SingleMethod.isRun = false;
        this.doTest("next.yaml", null);
        assertTrue(SingleMethod.isRun, "Should run function");
    }

    public static class NextProcedure {
        public void procedure() {
        }
    }

    @Test
    public void nextWithParameter() {
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


    /**
     * Undertakes the composition test.
     *
     * @param configurationFile Configuration of composition.
     * @param parameter Parameter to invoke the composition.
     */
    private void doTest(String configurationFile, Object parameter) {

        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Add the composition
            OfficeSection section = composeArchitect.addComposition("compose", configurationFile, new PropertyListImpl());

            // Allow invoking the composition
            OfficeSection trigger = office.addSection("trigger", Trigger.class);
            office.getOfficeArchitect().link(trigger.getOfficeSectionOutput("handle"), section.getOfficeSectionInput(ComposeArchitect.INPUT_NAME));
        });

        // Test
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

            // Invoke the composition
            CompileOfficeFloor.invokeProcess(officeFloor, "trigger.trigger", parameter);

        } catch (Throwable ex) {
            throw fail(ex);
        }
    }

    public static class Trigger {
        @Next("handle")
        public String trigger(@Parameter String parameter) {
            return parameter;
        }
    }

}
