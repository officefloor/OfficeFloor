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
import static org.junit.jupiter.api.Assertions.assertSame;

public class ComposeDirectoryTest {

    @Test
    public void directory() throws Throwable {

        // Compile capturing the items
        Map<String, OfficeSection> items = new HashMap<>();
        Map<String, OfficeSection> naming = new HashMap<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect architect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Load all the compositions
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            architect.addCompositions((context) -> {
                        OfficeSection section = context.getCompositionSection();
                        naming.put(context.getItemName(), section);
                        return section;
                    }, "directory", properties, ComposeConfiguration.class, items::put);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
        }

        // Ensure have directory of items
        assertComposition("one", items, naming);
        assertComposition("{two}", items, naming);
        assertComposition("sub/three.GET", items, naming);
        assertComposition("sub/{four}", items, naming);
        assertComposition("sub/five", items, naming);
        assertComposition("sub/five/six", items, naming);
        assertEquals(6, items.size(), "Incorrect number of items");
    }

    private static void assertComposition(String name, Map<String, OfficeSection> items, Map<String, OfficeSection> naming) {
        OfficeSection section = items.get(name);
        assertNotNull(section, "Should have section for name " + name);
        assertEquals(name, section.getOfficeSectionName(), "Incorrect section name");
        assertSame(section, naming.get(name), "Should have suggested name from file");
    }

    public static class DirectoryService {
        public void service() {
            // Nothing as confirming load of directory
        }
    }

}
