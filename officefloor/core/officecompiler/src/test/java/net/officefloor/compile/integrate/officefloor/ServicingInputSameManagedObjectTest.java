package net.officefloor.compile.integrate.officefloor;


import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServicingInputSameManagedObjectTest {

    @Test
    public void sameInputManagedObjectTypeForTwoExternalInputs() throws Throwable {

        // Capture the inputs
        Closure<ExternalServiceInput<ServiceInputObject, ServiceInputObject>> inputOne = new Closure<>();
        Closure<ExternalServiceInput<ServiceInputObject, ServiceInputObject>> inputTwo = new Closure<>();

        // Configure with same object input type to two different handlings
        final String SECTION_NAME = "SECTION";
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {
            context.addSection(SECTION_NAME, MockSection.class);
        });
        compile.officeFloor((context) -> {
            DeployedOffice office = context.getDeployedOffice();

            // Obtain the inputs
            inputOne.value = office.getDeployedOfficeInput(SECTION_NAME, "one")
                    .addExternalServiceInput(ServiceInputObject.class, ServiceInputObject.class);
            inputTwo.value = office.getDeployedOfficeInput(SECTION_NAME, "two")
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
