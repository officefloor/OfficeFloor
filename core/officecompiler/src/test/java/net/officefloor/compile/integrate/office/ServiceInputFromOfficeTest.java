package net.officefloor.compile.integrate.office;

import net.officefloor.compile.integrate.officefloor.ServicingInputSameManagedObjectTest;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServiceInputFromOfficeTest {

    @Test
    public void serviceInput() throws Throwable {

        // Capture the inputs
        Closure<ExternalServiceInput<ServiceInputObject, ServiceInputObject>> inputOne = new Closure<>();
        Closure<ExternalServiceInput<ServiceInputObject, ServiceInputObject>> inputTwo = new Closure<>();

        // Configure with same object input type to two different handlings
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {

            // Add the section
            OfficeSection section = context.addSection("SECTION", MockSection.class);

            // Create the external service inputs
            inputOne.value = section.getOfficeSectionInput("one")
                    .addExternalServiceInput(ServiceInputObject.class, ServiceInputObject.class);
            inputTwo.value = section.getOfficeSectionInput("two")
                    .addExternalServiceInput(ServiceInputObject.class, ServiceInputObject.class);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

            // Trigger first input
            ServiceInputObject one = new ServiceInputObject();
            inputOne.value.service(one, null);
            assertEquals("ONE", one.value, "Should invoke first flow");

            // Trigger second input
            ServiceInputObject two = new ServiceInputObject();
            inputTwo.value.service(two, null);
            assertEquals("TWO", two.value, "Should invoke second flow");
        }
    }

    public static class MockSection {

        public void one(ServiceInputObject input) {
            input.value = "ONE";
        }

        public void two(ServiceInputObject input) {
            input.value = "TWO";
        }
    }

    private static class ServiceInputObject implements InputManagedObject {

        private String value = null;

        @Override
        public Object getObject() throws Throwable {
            return this;
        }

        @Override
        public void clean(CleanupEscalation[] cleanupEscalations) throws Throwable {
            fail("Should have no escalations");
        }
    }

}
