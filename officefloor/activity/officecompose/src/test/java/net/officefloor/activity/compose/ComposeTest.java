package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import org.junit.jupiter.api.Test;

import static net.officefloor.frame.test.OfficeFrameTestCase.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComposeTest {

    @Test
    public void single() {

        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Add the section
            OfficeSection section = composeArchitect.addComposition("compose", "single.yaml", new PropertyListImpl());
        });

        // Test
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

            // Invoke the composition
            CompileOfficeFloor.invokeProcess(officeFloor, "compose.method.procedure", null);

        } catch (Throwable ex) {
            throw fail(ex);
        }

        assertTrue(SingleFunction.isRun, "Should run function");
    }

    public static class SingleFunction {

        private static boolean isRun = false;

        public void procedure() {
            isRun = true;
        }
    }

}
