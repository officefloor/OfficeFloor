package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComposeInputTest {

    @Test
    public void next() throws Throwable {
        this.doTest("next.yml", "NEXT");
    }

    public static class NextService {
        public String service() {
            return "NEXT";
        }
    }

    @Test
    public void output() throws Throwable {
        this.doTest("output.yml", "OUTPUT");
    }

    @FunctionalInterface
    public static interface OutputFlow {
        void flow(String parameter);
    }

    public static class OutputService {
        public void service(@Flow("flow") OutputFlow flow) {
            flow.flow("OUTPUT");
        }
    }

    protected void doTest(String resourceName, String expectedParameter) throws Throwable {
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        Closure<ExternalServiceInput<MockManagedObject, MockManagedObject>> externalInput = new Closure<>();
        compiler.office((office) -> {
            OfficeArchitect officeArchitect = office.getOfficeArchitect();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, office.getOfficeSourceContext());

            // Create and register the input
            OfficeSectionInput input = office.addSection("input", InputService.class).getOfficeSectionInput("service");
            composeArchitect.addInput("input", input);

            // Add the composition
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            externalInput.value = composeArchitect.addComposition("compose",
                    (context) -> context.getStartFunction().addExternalServiceInput(MockManagedObject.class, MockManagedObject.class),
                    "input/" + resourceName, properties, ComposeConfiguration.class);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            InputService.parameter = null;
            externalInput.value.service(new MockManagedObject(), null);
            assertEquals(expectedParameter, InputService.parameter, "Should invoke input service");
        }
    }

    public static class InputService {
        private static String parameter;
        public void service(@Parameter String parameter) {
            InputService.parameter = parameter;
        }
    }

    public static class MockManagedObject implements InputManagedObject {

        @Override
        public void clean(CleanupEscalation[] cleanupEscalations) throws Throwable {
            // Nothing to clean
        }

        @Override
        public Object getObject() throws Throwable {
            return this;
        }
    }

}
