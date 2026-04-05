package net.officefloor.activity.govern;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import org.junit.jupiter.api.Test;

public class GovernanceTest {

    @Test
    public void simple() throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((office) -> {

            

        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

        }
    }

    public static class SimpleGovernance {
    }

    public static class MockManagedObject {

    }

}
