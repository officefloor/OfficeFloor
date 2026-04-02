package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Ensure can administer {@link net.officefloor.frame.api.function.ManagedFunction} reflectively via annotations.
 */
@ExtendWith(TestSupportExtension.class)
public class AdministerManagedFunctionTest {

    private final ConstructTestSupport construct = new ConstructTestSupport();

    /**
     * Undertakes {@link net.officefloor.frame.api.administration.Administration} before the
     * {@link net.officefloor.frame.api.function.ManagedFunction}.
     */
    @Test
    public void preAdministration() throws Exception {

        final String ANNOTATION = "ANNOTATION";

        // Construct the functions
        TestWork work = new TestWork();
        this.construct.constructFunction(work, "trigger").setNextFunction("task");
        ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
        task.getBuilder().addAnnotation(ANNOTATION);
        task.setNextFunction("complete");
        this.construct.constructFunction(work, "complete");

        // Construct the administration
        task.preAdminister("preTask").buildAdministrationContext();

        // Ensure undertakes administration before
        this.construct.invokeFunctionAndValidate("trigger", null, "trigger", "preTask", "task", "complete");

        // Ensure able to administer based on annotations
        assertNotNull(work.preAdministrationAnnotations, "Should have annotations");
        assertEquals(1, work.preAdministrationAnnotations.length, "Incorrect number of annotations");
        assertSame(ANNOTATION, work.preAdministrationAnnotations[0], "Incorrect annotation");
    }

    /**
     * Undertakes {@link net.officefloor.frame.api.administration.Administration} after the
     * {@link net.officefloor.frame.api.function.ManagedFunction}.
     */
    @Test
    public void postAdministration() throws Exception {

        final String ANNOTATION = "ANNOTATION";

        // Construct the functions
        TestWork work = new TestWork();
        this.construct.constructFunction(work, "trigger").setNextFunction("task");
        ReflectiveFunctionBuilder task = this.construct.constructFunction(work, "task");
        task.getBuilder().addAnnotation(ANNOTATION);
        task.setNextFunction("complete");
        this.construct.constructFunction(work, "complete");

        // Construct the administration
        task.postAdminister("postTask").buildAdministrationContext();

        // Ensure undertakes administration before
        this.construct.invokeFunctionAndValidate("trigger", null, "trigger", "task", "postTask", "complete");

        // Ensure able to administer based on annotations
        assertNotNull(work.postAdministrationAnnotations, "Should have annotations");
        assertEquals(1, work.postAdministrationAnnotations.length, "Incorrect number of annotations");
        assertSame(ANNOTATION, work.postAdministrationAnnotations[0], "Incorrect annotation");
    }

    /**
     * Test functionality.
     */
    public class TestWork {

        public Object[] preAdministrationAnnotations = null;

        public Object[] postAdministrationAnnotations = null;

        public void trigger() {
        }

        public void preTask(Object[] extensions, AdministrationContext<?, ?, ?> context) {
            assertNull(this.preAdministrationAnnotations, "Should only be invoked once");
            this.preAdministrationAnnotations = context.getManagedFunctionAnnotations();
        }

        public void task() {
        }

        public void postTask(Object[] extensions, AdministrationContext<?, ?, ?> context) {
            assertNull(this.preAdministrationAnnotations, "Should only be invoked once");
            this.postAdministrationAnnotations = context.getManagedFunctionAnnotations();
        }

        public void complete() {
        }
    }

}
