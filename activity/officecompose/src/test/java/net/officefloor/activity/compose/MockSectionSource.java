package net.officefloor.activity.compose;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockSectionSource extends AbstractSectionSource {

    /*
     * =================== SectionSource =====================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
        assertEquals("mock", context.getSectionLocation());
    }
}
