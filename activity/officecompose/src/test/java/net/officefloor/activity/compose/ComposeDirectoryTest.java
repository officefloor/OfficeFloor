package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ComposeDirectoryTest {

    @Test
    public void directory() throws Throwable {

        // Compile capturing the items
        Map<String, OfficeSection> items = new HashMap<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect architect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Load all the compositions
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            architect.addCompositions(ComposeContext::getCompositionSection,
                    "directory", properties, ComposeConfiguration.class, items::put);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
        }

        // Ensure have directory of items
        assertComposition("one", items);
        assertComposition("{two}", items);
        assertComposition("sub/three.GET", items);
        assertComposition("sub/{four}", items);
        assertComposition("sub/five", items);
        assertComposition("sub/five/six", items);
        assertEquals(6, items.size(), "Incorrect number of items");
    }

    private static void assertComposition(String name, Map<String, OfficeSection> items) {
        OfficeSection section = items.get(name);
        assertNotNull(section, "Should have section for name " + name);
        assertEquals(name, section.getOfficeSectionName(), "Incorrect section name");
    }

    public static class DirectoryService {
        public void service() {
            // Nothing as confirming load of directory
        }
    }

}
